package mingovvv.turnstile.dto.response;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.Reservation;
import mingovvv.turnstile.domain.enums.ReservationStatus;

import java.time.LocalDateTime;

/**
 * 예약 응답
 */
@Getter
@Builder
public class ReservationResponse {

    private String reservationId;
    private String eventId;
    private String seatId;
    private String userId;
    private String paymentId;
    private int amount;
    private ReservationStatus status;
    private String statusDescription;
    private LocalDateTime confirmedAt;

    public static ReservationResponse from(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getReservationId())
                .eventId(reservation.getEventId())
                .seatId(reservation.getSeatId())
                .userId(reservation.getUserId())
                .paymentId(reservation.getPaymentId())
                .amount(reservation.getAmount())
                .status(reservation.getStatus())
                .statusDescription(reservation.getStatus().getDescription())
                .confirmedAt(reservation.getConfirmedAt())
                .build();
    }
}
