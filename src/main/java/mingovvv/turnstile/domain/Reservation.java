package mingovvv.turnstile.domain;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.enums.ReservationStatus;

import java.time.LocalDateTime;

/**
 * 예약 도메인
 */
@Getter
@Builder
public class Reservation {

    private String reservationId;
    private String eventId;
    private String seatId;
    private String userId;
    private String paymentId;
    private int amount;
    private ReservationStatus status;
    private LocalDateTime confirmedAt;

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isConfirmed() {
        return this.status == ReservationStatus.CONFIRMED;
    }

    /**
     * 복합 키 생성 (eventId:seatId)
     */
    public String getSeatCompositeKey() {
        return eventId + ":" + seatId;
    }
}
