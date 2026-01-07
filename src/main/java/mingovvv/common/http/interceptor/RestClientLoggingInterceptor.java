package mingovvv.common.http.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RestClientLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_LOG_LENGTH = 1000; // 로그 길이 제한 상수화

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        logRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        return new BufferedClientHttpResponse(response);
    }

    private void logRequest(HttpRequest request, byte[] body) {
        log.info("---> [OUT] Method: {}, URI: {}", request.getMethod(), request.getURI());
        log.info("---> [OUT] Headers: {}", request.getHeaders());

        if (body != null && body.length > 0) {
            String bodyString = new String(body, StandardCharsets.UTF_8);
            if (bodyString.length() > MAX_LOG_LENGTH) {
                log.info("---> [OUT] Body: {} ... [TRUNCATED]", bodyString.substring(0, MAX_LOG_LENGTH));
            } else {
                log.info("---> [OUT] Body: {}", bodyString);
            }
        }
    }

    private static class BufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private final byte[] body;

        public BufferedClientHttpResponse(ClientHttpResponse response) throws IOException {
            this.response = response;
            this.body = response.getBody().readAllBytes();

            logResponse();
        }

        private void logResponse() {
            try {
                log.info("<--- [IN] Status: {} {}", response.getStatusCode().value(), response.getStatusText());
                log.info("<--- [IN] Headers: {}", response.getHeaders());

                if (body != null && body.length > 0) {
                    String bodyContent = new String(body, StandardCharsets.UTF_8);
                    if (bodyContent.length() > MAX_LOG_LENGTH) {
                        log.info("<--- [IN] Body: {} ... [TRUNCATED - {} chars]", bodyContent.substring(0, MAX_LOG_LENGTH), bodyContent.length());
                    } else {
                        log.info("<--- [IN] Body: {}", bodyContent);
                    }
                }
            } catch (Exception e) {
                log.warn("Response logging failed.", e);
            }
        }

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public void close() {
            response.close();
        }

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return response.getHeaders();
        }
    }

}
