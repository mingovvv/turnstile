package mingovvv.turnstile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대기열 진입 요청
 */
@Getter
@NoArgsConstructor
public class QueueEntryRequest {

    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    public QueueEntryRequest(String userId) {
        this.userId = userId;
    }
}
