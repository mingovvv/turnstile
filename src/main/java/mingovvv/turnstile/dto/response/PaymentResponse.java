package mingovvv.turnstile.dto.response;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.Payment;
import mingovvv.turnstile.domain.enums.PaymentStatus;

import java.time.LocalDateTime;

/**
 * 결제 응답
 */
@Getter
@Builder
public class PaymentResponse {

    private String paymentId;
    private String reservationId;
    private String userId;
    private int amount;
    private PaymentStatus status;
    private String statusDescription;
    private LocalDateTime paidAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .reservationId(payment.getReservationId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .statusDescription(payment.getStatus().getDescription())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
