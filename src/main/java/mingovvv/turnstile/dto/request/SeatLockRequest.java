package mingovvv.turnstile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 좌석 선점 요청
 */
@Getter
@NoArgsConstructor
public class SeatLockRequest {

    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    public SeatLockRequest(String userId) {
        this.userId = userId;
    }
}
