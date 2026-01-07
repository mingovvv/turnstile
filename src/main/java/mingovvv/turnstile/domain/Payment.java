package mingovvv.turnstile.domain;

import lombok.Builder;
import lombok.Getter;
import mingovvv.turnstile.domain.enums.PaymentStatus;

import java.time.LocalDateTime;

/**
 * 결제 도메인 (Mock)
 */
@Getter
@Builder
public class Payment {

    private String paymentId;
    private String userId;
    private String reservationId;
    private int amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    public void success() {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }
}
