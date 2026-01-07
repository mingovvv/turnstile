package mingovvv.common.ratelimit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "feature.rate-limit")
public class RateLimitProperties {

    /**
     * 레이트 리밋 필터 on/off 스위치입니다.
     */
    private boolean enabled = false;

    /**
     * 윈도우당 허용 요청 수입니다.
     */
    private int maxRequests = 100;

    /**
     * 윈도우 크기(초)입니다.
     */
    private int windowSeconds = 60;

    /**
     * 레이트 리밋 사용 여부를 설정합니다.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 윈도우당 최대 요청 수를 설정합니다.
     */
    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    /**
     * 윈도우 크기(초)를 설정합니다.
     */
    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

}
