package mingovvv.common.http.config;

import lombok.RequiredArgsConstructor;
import mingovvv.common.http.client.TestServerClient;
import mingovvv.common.http.interceptor.RestClientLoggingInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final RestClientLoggingInterceptor restClientLoggingInterceptor;

    /**
     * TestServerClient RestClient 설정입니다.
     * Resilience4j 인터셉터가 등록돼 있으면 클라이언트별로 인스턴스를 선택해 적용합니다.
     */
    @Bean
    public TestServerClient testServerClient(
            @Value("${api.test-client.url}") String url,
            @Value("${api.test-client.connection-timeout:1s}") Duration connectionTimeout,
            @Value("${api.test-client.read-timeout:10s}") Duration readTimeout,
            @Value("${api.test-client.api-key}") String apiKey,
            @Value("${api.test-client.resilience4j.instance-name:default}") String resilienceInstanceName,
            ObjectProvider<Resilience4jRestClientInterceptorFactory> interceptorFactory
    ) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

        interceptors.add(restClientLoggingInterceptor);

        Resilience4jRestClientInterceptorFactory factory = interceptorFactory.getIfAvailable();
        if (factory != null) {
            interceptors.add(factory.create(resilienceInstanceName));
        }

        return new RestClientBuilder()
            .url(url)
            .headers("Authorization", "Bearer " + apiKey)
            .connectionTimeout(connectionTimeout)
            .readTimeout(readTimeout)
            .requestInterceptors(interceptors.toArray(new ClientHttpRequestInterceptor[0]))
            .build(TestServerClient.class);
    }

}
