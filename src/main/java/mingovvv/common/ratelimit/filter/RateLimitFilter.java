package mingovvv.common.ratelimit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import mingovvv.common.constants.ResultCode;
import mingovvv.common.model.BaseResponse;
import mingovvv.common.model.BaseResponseFactory;
import mingovvv.common.ratelimit.RateLimitKeyResolver;
import mingovvv.common.ratelimit.RateLimiter;
import mingovvv.common.ratelimit.config.RateLimitProperties;
import mingovvv.common.ratelimit.model.RateLimitResult;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final RateLimiter rateLimiter;
    private final RateLimitKeyResolver keyResolver;
    private final ObjectMapper objectMapper;

    /**
     * 레이트 리밋을 적용하고 초과 시 429로 차단합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 비활성화 시에는 바로 통과시켜 오버헤드를 줄입니다.
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = keyResolver.resolve(request);
        if (key == null || key.isBlank()) {
            key = "unknown";
        }

        RateLimitResult result = rateLimiter.tryConsume(key);

        response.setHeader("X-RateLimit-Limit", String.valueOf(properties.getMaxRequests()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(result.resetEpochSeconds()));

        if (!result.allowed()) {
            long nowEpochSeconds = Instant.now().getEpochSecond();
            long retryAfter = Math.max(0, result.resetEpochSeconds() - nowEpochSeconds);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            writeRateLimitedResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 레이트 리밋 초과 시 표준 에러 응답을 내려줍니다.
     */
    private void writeRateLimitedResponse(HttpServletResponse response) throws IOException {
        BaseResponse<Void> errorResponse = BaseResponseFactory.create(ResultCode.Error.REQ_RATE_LIMITED);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

}
