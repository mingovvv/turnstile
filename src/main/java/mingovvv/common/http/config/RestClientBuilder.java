package mingovvv.common.http.config;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.util.*;

public class RestClientBuilder {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private String baseUrl;
    private Duration connectionTimeout = DEFAULT_TIMEOUT;
    private Duration readTimeout = DEFAULT_TIMEOUT;
    private List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();

    private final Map<String, String> headers = new HashMap<>();

    public RestClientBuilder url(String url) {
        this.baseUrl = url;
        return this;
    }

    public RestClientBuilder connectionTimeout(Duration duration) {
        this.connectionTimeout = duration;
        return this;
    }

    public RestClientBuilder readTimeout(Duration duration) {
        this.readTimeout = duration;
        return this;
    }

    public RestClientBuilder requestInterceptors(ClientHttpRequestInterceptor... interceptors) {
        this.interceptors = Arrays.asList(interceptors);
        return this;
    }

    public RestClientBuilder headers(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Headers must be key-value pairs");
        }
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            headers.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return this;
    }

    public <T> T build(Class<T> clientClass) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setConnectionRequestTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);

        var builder = RestClient.builder()
            .requestInterceptors(clientHttpRequestInterceptors -> clientHttpRequestInterceptors.addAll(interceptors))
            .baseUrl(baseUrl)
            .requestFactory(factory);

        // 헤더 일괄 적용
        if (!headers.isEmpty()) {
            builder.defaultHeaders(httpHeaders ->
                headers.forEach(httpHeaders::add)
            );
        }

        RestClient restClient = builder.build();

        return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build()
            .createClient(clientClass);
    }

}
