package mingovvv.turnstile.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE Emitter 관리 레지스트리
 * <p>
 * 대기열에 있는 사용자들의 SSE 연결을 관리합니다.
 * Key: eventId:userId
 */
@Slf4j
@Component
public class QueueSseEmitterRegistry {

    // eventId:userId → SseEmitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분

    /**
     * SSE Emitter 등록
     */
    public SseEmitter register(String eventId, String userId) {
        String key = compositeKey(eventId, userId);

        // 기존 연결이 있으면 제거
        removeIfExists(key);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 연결 종료 시 정리
        emitter.onCompletion(() -> {
            log.debug("SSE completed: {}", key);
            emitters.remove(key);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE timeout: {}", key);
            emitters.remove(key);
        });

        emitter.onError(e -> {
            log.debug("SSE error: {}, error: {}", key, e.getMessage());
            emitters.remove(key);
        });

        emitters.put(key, emitter);
        log.debug("SSE registered: {}", key);

        return emitter;
    }

    /**
     * 특정 사용자에게 이벤트 전송
     */
    public void send(String eventId, String userId, QueueSseEvent event) {
        String key = compositeKey(eventId, userId);
        SseEmitter emitter = emitters.get(key);

        if (emitter == null) {
            log.debug("No SSE emitter found: {}", key);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(event.getEventType())
                    .data(event));
            log.debug("SSE sent: key={}, type={}", key, event.getEventType());
        } catch (IOException e) {
            log.warn("Failed to send SSE: {}, error: {}", key, e.getMessage());
            emitters.remove(key);
        }
    }

    /**
     * 특정 사용자의 연결 종료
     */
    public void complete(String eventId, String userId) {
        String key = compositeKey(eventId, userId);
        SseEmitter emitter = emitters.remove(key);

        if (emitter != null) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Error completing SSE: {}", key);
            }
        }
    }

    /**
     * 연결된 Emitter 수 조회
     */
    public int getEmitterCount() {
        return emitters.size();
    }

    /**
     * 특정 이벤트의 Emitter 수 조회
     */
    public long getEmitterCountByEventId(String eventId) {
        String prefix = eventId + ":";
        return emitters.keySet().stream()
                .filter(key -> key.startsWith(prefix))
                .count();
    }

    private void removeIfExists(String key) {
        SseEmitter existing = emitters.remove(key);
        if (existing != null) {
            try {
                existing.complete();
            } catch (Exception ignored) {
            }
        }
    }

    private String compositeKey(String eventId, String userId) {
        return eventId + ":" + userId;
    }
}
