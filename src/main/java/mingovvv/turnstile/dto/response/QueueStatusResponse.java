package mingovvv.turnstile.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 대기열 상태 응답
 */
@Getter
@Builder
public class QueueStatusResponse {

    private String eventId;
    private String userId;
    private long position;           // 현재 순번 (0이면 입장 가능)
    private long totalWaiting;       // 전체 대기 인원
    private int estimatedWaitSeconds; // 예상 대기 시간 (초)
    private boolean canEnter;        // 입장 가능 여부
    private String token;            // 입장 토큰 (입장 가능 시)

    public static QueueStatusResponse waiting(String eventId, String userId, long position, long totalWaiting, int estimatedWaitSeconds) {
        return QueueStatusResponse.builder()
                .eventId(eventId)
                .userId(userId)
                .position(position)
                .totalWaiting(totalWaiting)
                .estimatedWaitSeconds(estimatedWaitSeconds)
                .canEnter(false)
                .token(null)
                .build();
    }

    public static QueueStatusResponse canEnter(String eventId, String userId, String token) {
        return QueueStatusResponse.builder()
                .eventId(eventId)
                .userId(userId)
                .position(0)
                .totalWaiting(0)
                .estimatedWaitSeconds(0)
                .canEnter(true)
                .token(token)
                .build();
    }

    public static QueueStatusResponse notInQueue(String eventId, String userId) {
        return QueueStatusResponse.builder()
                .eventId(eventId)
                .userId(userId)
                .position(-1)
                .totalWaiting(0)
                .estimatedWaitSeconds(0)
                .canEnter(false)
                .token(null)
                .build();
    }
}
