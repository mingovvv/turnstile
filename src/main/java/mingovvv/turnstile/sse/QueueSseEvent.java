package mingovvv.turnstile.sse;

import lombok.Builder;
import lombok.Getter;

/**
 * SSE 이벤트 데이터
 */
@Getter
@Builder
public class QueueSseEvent {

    /**
     * 이벤트 타입
     * - QUEUE_UPDATE: 순번 업데이트
     * - TOKEN_ISSUED: 토큰 발급 (입장 가능)
     * - QUEUE_LEFT: 대기열 이탈
     */
    private String eventType;

    private String eventId;
    private String userId;
    private Long position;           // 현재 순번
    private Long totalWaiting;       // 전체 대기 인원
    private Integer estimatedWaitSeconds;
    private Boolean canEnter;
    private String token;            // 토큰 (TOKEN_ISSUED 시)
    private String message;

    public static QueueSseEvent queueUpdate(String eventId, String userId, long position, long totalWaiting, int estimatedWaitSeconds) {
        return QueueSseEvent.builder()
                .eventType("QUEUE_UPDATE")
                .eventId(eventId)
                .userId(userId)
                .position(position)
                .totalWaiting(totalWaiting)
                .estimatedWaitSeconds(estimatedWaitSeconds)
                .canEnter(false)
                .build();
    }

    public static QueueSseEvent tokenIssued(String eventId, String userId, String token) {
        return QueueSseEvent.builder()
                .eventType("TOKEN_ISSUED")
                .eventId(eventId)
                .userId(userId)
                .position(0L)
                .canEnter(true)
                .token(token)
                .message("입장 가능합니다. 좌석을 선택해주세요.")
                .build();
    }

    public static QueueSseEvent queueLeft(String eventId, String userId) {
        return QueueSseEvent.builder()
                .eventType("QUEUE_LEFT")
                .eventId(eventId)
                .userId(userId)
                .message("대기열에서 이탈하였습니다.")
                .build();
    }
}
