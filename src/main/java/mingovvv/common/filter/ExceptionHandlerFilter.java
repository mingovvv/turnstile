package mingovvv.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 필터 체인에서 발생하는 예외를 처리하는 필터입니다.
 * <p>
 * 필터 레벨에서 발생한 예외를 GlobalExceptionHandler로 전달하여
 * 일관된 예외 응답 포맷을 제공합니다.
 * <p>
 * 실행 순서: HIGHEST_PRECEDENCE (가장 먼저 실행)
 * - ExceptionHandlerFilter → AccessLogFilter → 기타 필터들
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ExceptionHandlerFilter extends OncePerRequestFilter implements Ordered {

    private final HandlerExceptionResolver handlerExceptionResolver;

    public ExceptionHandlerFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error occurred in filter chain : {}", e.getMessage(), e);
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
