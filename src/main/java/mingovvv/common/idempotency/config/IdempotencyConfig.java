package mingovvv.common.idempotency.config;

import mingovvv.common.idempotency.IdempotencyStore;
import mingovvv.common.idempotency.filter.IdempotencyFilter;
import mingovvv.common.idempotency.impl.InMemoryIdempotencyStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IdempotencyProperties.class)
public class IdempotencyConfig {

    /**
     * 외부 스토어가 없을 때 사용하는 기본 인메모리 구현입니다.
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotencyStore idempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    /**
     * enabled=true인 경우에만 아이템포턴시 필터를 등록합니다.
     */
    @Bean
    @ConditionalOnProperty(prefix = "feature.idempotency", name = "enabled", havingValue = "true")
    public IdempotencyFilter idempotencyFilter(IdempotencyProperties properties, IdempotencyStore store) {
        return new IdempotencyFilter(properties, store);
    }

}
