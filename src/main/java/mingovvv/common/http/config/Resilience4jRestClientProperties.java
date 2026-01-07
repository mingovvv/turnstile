package mingovvv.common.http.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
/**
 * RestClient용 Resilience4j 설정 프로퍼티입니다.
 */
@ConfigurationProperties(prefix = "feature.resilience4j.rest-client")
public class Resilience4jRestClientProperties {

    /**
     * Retry 활성화 여부입니다.
     */
    private boolean retryEnabled = true;

    /**
     * CircuitBreaker 활성화 여부입니다.
     */
    private boolean circuitBreakerEnabled = true;

    /**
     * 기본 인스턴스 이름입니다.
     */
    private String instanceName = "default";

    /**
     * Retry 사용 여부를 설정합니다.
     */
    public void setRetryEnabled(boolean retryEnabled) {
        this.retryEnabled = retryEnabled;
    }

    /**
     * CircuitBreaker 사용 여부를 설정합니다.
     */
    public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
        this.circuitBreakerEnabled = circuitBreakerEnabled;
    }

    /**
     * 기본 인스턴스 이름을 설정합니다.
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

}
