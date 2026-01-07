package mingovvv.turnstile.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 좌석 선점 응답
 */
@Getter
@Builder
public class SeatLockResponse {

    private String eventId;
    private String seatId;
    private String userId;
    private boolean locked;
    private int expiresInSeconds;   // 남은 선점 시간 (초)
    private String reason;          // 실패 시 사유

    public static SeatLockResponse success(String eventId, String seatId, String userId, int expiresInSeconds) {
        return SeatLockResponse.builder()
                .eventId(eventId)
                .seatId(seatId)
                .userId(userId)
                .locked(true)
                .expiresInSeconds(expiresInSeconds)
                .reason(null)
                .build();
    }

    public static SeatLockResponse alreadyOwned(String eventId, String seatId, String userId, int remainingSeconds) {
        return SeatLockResponse.builder()
                .eventId(eventId)
                .seatId(seatId)
                .userId(userId)
                .locked(true)
                .expiresInSeconds(remainingSeconds)
                .reason("ALREADY_OWNED")
                .build();
    }

    public static SeatLockResponse failed(String eventId, String seatId, String reason) {
        return SeatLockResponse.builder()
                .eventId(eventId)
                .seatId(seatId)
                .userId(null)
                .locked(false)
                .expiresInSeconds(0)
                .reason(reason)
                .build();
    }
}
