package mingovvv.common.ratelimit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import mingovvv.common.ratelimit.RateLimitKeyResolver;
import mingovvv.common.ratelimit.RateLimiter;
import mingovvv.common.ratelimit.filter.RateLimitFilter;
import mingovvv.common.ratelimit.impl.InMemoryRateLimiter;
import mingovvv.common.ratelimit.impl.IpRateLimitKeyResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    /**
     * 커스텀 Resolver가 없을 때 사용하는 기본 Resolver입니다.
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitKeyResolver rateLimitKeyResolver() {
        return new IpRateLimitKeyResolver();
    }

    /**
     * 외부 스토어가 없을 때 사용하는 기본 인메모리 구현입니다.
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimiter rateLimiter(RateLimitProperties properties) {
        return new InMemoryRateLimiter(properties);
    }

    /**
     * enabled=true인 경우에만 레이트 리밋 필터를 등록합니다.
     */
    @Bean
    @ConditionalOnProperty(prefix = "feature.rate-limit", name = "enabled", havingValue = "true")
    public RateLimitFilter rateLimitFilter(
        RateLimitProperties properties,
        RateLimiter rateLimiter,
        RateLimitKeyResolver keyResolver,
        ObjectMapper objectMapper
    ) {
        return new RateLimitFilter(properties, rateLimiter, keyResolver, objectMapper);
    }

}
