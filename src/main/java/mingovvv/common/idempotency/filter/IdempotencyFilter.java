package mingovvv.common.idempotency.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mingovvv.common.idempotency.IdempotencyStore;
import mingovvv.common.idempotency.config.IdempotencyProperties;
import mingovvv.common.idempotency.model.CachedResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Optional;

@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyProperties properties;
    private final IdempotencyStore store;

    /**
     * 동일 아이템포턴시 키 요청에 대해 캐시된 응답을 재사용합니다.
     * 설정된 HTTP 메서드에만 적용됩니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 비활성화 또는 미대상 메서드는 바로 통과시킵니다.
        if (!properties.isEnabled() || !properties.getMethods().contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getHeader(properties.getHeaderName());
        if (!StringUtils.hasText(key)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<CachedResponse> cached = store.get(key);
        if (cached.isPresent()) {
            writeCachedResponse(response, cached.get());
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        int status = responseWrapper.getStatus();
        if (HttpStatus.valueOf(status).is2xxSuccessful()) {
            CachedResponse responseBody = new CachedResponse(
                    status,
                    responseWrapper.getContentType(),
                    responseWrapper.getContentAsByteArray()
            );
            store.putIfAbsent(key, responseBody, properties.getTtlSeconds());
        }

        responseWrapper.copyBodyToResponse();
    }

    /**
     * 캐시된 응답을 그대로 내려줍니다.
     */
    private void writeCachedResponse(HttpServletResponse response, CachedResponse cached) throws IOException {
        response.setStatus(cached.status());
        if (cached.contentType() != null) {
            response.setContentType(cached.contentType());
        }
        response.getOutputStream().write(cached.body());
    }

}
