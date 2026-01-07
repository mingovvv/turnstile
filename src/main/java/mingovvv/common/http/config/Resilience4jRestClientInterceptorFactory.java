package mingovvv.common.http.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import mingovvv.common.http.interceptor.Resilience4jRestClientInterceptor;
import org.springframework.stereotype.Component;

/**
 * RestClient용 Resilience4j 인터셉터를 생성하는 팩토리입니다.
 */
@Component
public class Resilience4jRestClientInterceptorFactory {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final Resilience4jRestClientProperties properties;

    public Resilience4jRestClientInterceptorFactory(
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            Resilience4jRestClientProperties properties
    ) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.properties = properties;
    }

    /**
     * 인스턴스 이름에 맞는 Resilience4j 인터셉터를 생성합니다.
     * 인스턴스 이름이 비어있으면 기본값을 사용합니다.
     *
     * @param instanceName Resilience4j 인스턴스 이름
     * @return 인터셉터 인스턴스
     */
    public Resilience4jRestClientInterceptor create(String instanceName) {
        String resolvedName = (instanceName == null || instanceName.isBlank()) ? properties.getInstanceName() : instanceName;
        return new Resilience4jRestClientInterceptor(
                circuitBreakerRegistry,
                retryRegistry,
                resolvedName,
                properties.isRetryEnabled(),
                properties.isCircuitBreakerEnabled()
        );
    }

}
