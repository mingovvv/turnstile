package mingovvv.turnstile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 요청
 */
@Getter
@NoArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    @NotBlank(message = "이벤트 ID는 필수입니다.")
    private String eventId;

    @NotBlank(message = "좌석 ID는 필수입니다.")
    private String seatId;

    public PaymentRequest(String userId, String eventId, String seatId) {
        this.userId = userId;
        this.eventId = eventId;
        this.seatId = seatId;
    }
}
