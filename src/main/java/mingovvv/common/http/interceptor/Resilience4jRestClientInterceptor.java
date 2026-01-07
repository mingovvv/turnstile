package mingovvv.common.http.interceptor;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * RestClient 요청에 Resilience4j(리트라이/서킷브레이커)를 적용하는 인터셉터입니다.
 */
public class Resilience4jRestClientInterceptor implements ClientHttpRequestInterceptor {

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final boolean retryEnabled;
    private final boolean circuitBreakerEnabled;

    /**
     * 인스턴스 이름에 해당하는 Resilience4j 설정을 주입합니다.
     *
     * @param circuitBreakerRegistry 서킷 브레이커 레지스트리
     * @param retryRegistry 리트라이 레지스트리
     * @param instanceName 적용할 인스턴스 이름
     * @param retryEnabled 리트라이 활성화 여부
     * @param circuitBreakerEnabled 서킷 브레이커 활성화 여부
     */
    public Resilience4jRestClientInterceptor(CircuitBreakerRegistry circuitBreakerRegistry,
                                             RetryRegistry retryRegistry,
                                             String instanceName,
                                             boolean retryEnabled,
                                             boolean circuitBreakerEnabled) {
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceName);
        this.retry = retryRegistry.retry(instanceName);
        this.retryEnabled = retryEnabled;
        this.circuitBreakerEnabled = circuitBreakerEnabled;
    }

    /**
     * 요청 실행 시 Retry/CB 체인을 적용합니다.
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        CheckedSupplier<ClientHttpResponse> supplier = () -> execution.execute(request, body);
        // 설정에 따라 Retry/CB를 선택적으로 적용합니다.
        if (retryEnabled) {
            supplier = Retry.decorateCheckedSupplier(retry, supplier);
        }
        if (circuitBreakerEnabled) {
            supplier = CircuitBreaker.decorateCheckedSupplier(circuitBreaker, supplier);
        }
        try {
            return supplier.get();
        } catch (Exception ex) {
            // IOException은 그대로 던지고, 나머지는 런타임 예외를 보존합니다.
            if (ex instanceof IOException io) {
                throw io;
            }
            if (ex instanceof RuntimeException re) {
                throw re;
            }
            throw new IOException(ex);
        }
    }

}
