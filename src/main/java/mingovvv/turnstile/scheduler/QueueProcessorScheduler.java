package mingovvv.turnstile.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.domain.Event;
import mingovvv.turnstile.repository.memory.EventMemoryRepository;
import mingovvv.turnstile.repository.redis.TokenRedisRepository;
import mingovvv.turnstile.service.QueueService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 대기열 처리 스케줄러
 * <p>
 * 주기적으로 대기열에서 사용자를 꺼내 입장 토큰을 발급합니다.
 * 동시 입장 인원(maxConcurrentUsers)을 초과하지 않도록 제어합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueProcessorScheduler {

    private final QueueService queueService;
    private final EventMemoryRepository eventRepository;
    private final TokenRedisRepository tokenRepository;

    /**
     * 10초마다 대기열 처리
     * 각 이벤트별로 (maxConcurrentUsers - 현재 토큰 보유자 수)만큼 입장 처리
     */
    @Scheduled(fixedRate = 10000)
    public void processQueue() {
        List<Event> openEvents = eventRepository.findAll().stream()
                .filter(Event::isOpen)
                .toList();

        for (Event event : openEvents) {
            try {
                processEventQueue(event);
            } catch (Exception e) {
                log.error("Failed to process queue for event: {}", event.getEventId(), e);
            }
        }
    }

    private void processEventQueue(Event event) {
        String eventId = event.getEventId();
        int maxConcurrent = event.getMaxConcurrentUsers();

        // 현재 토큰 보유자 수 (= 좌석 선택 페이지에 있는 사용자 수)
        long currentTokenCount = tokenRepository.countByEventId(eventId);

        // 입장 가능 인원 계산
        int availableSlots = (int) (maxConcurrent - currentTokenCount);

        if (availableSlots <= 0) {
            log.debug("Queue full: eventId={}, maxConcurrent={}, currentTokens={}",
                    eventId, maxConcurrent, currentTokenCount);
            return;
        }

        // 대기열에서 availableSlots만큼 입장 처리
        int processed = queueService.processQueue(eventId, availableSlots);

        if (processed > 0) {
            log.info("Queue processed: eventId={}, processed={}, currentTokens={}/{}",
                    eventId, processed, currentTokenCount + processed, maxConcurrent);
        }
    }
}
