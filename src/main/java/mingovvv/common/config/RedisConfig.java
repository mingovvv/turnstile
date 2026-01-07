package mingovvv.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.common.utils.MaskingUtil;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

/**
 * Redis 설정을 관리하는 Configuration 클래스.
 * <p>
 * Upstash Redis와 연결하여 데이터를 캐싱하고 저장합니다.
 * RedisTemplate을 Bean으로 등록해 애플리케이션 전역에서 Redis 작업을 수행합니다.
 * <p>
 * 연결 정보는 application.yml에서 관리합니다.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    private final RedisProperties redisProperties;
    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * 객체 저장용 RedisTemplate Bean을 생성합니다.
     * <p>
     * 직렬화 설정:
     * - Key: String with prefix "turnstile:" (Custom PrefixStringRedisSerializer)
     * - Value: JSON without @class type info (GenericJackson2JsonRedisSerializer)
     * - Hash Key: String with prefix
     * - Hash Value: JSON without @class type info
     * <p>
     * 보안 강화: @class 타입 정보를 제거하여 역직렬화 공격 방지
     * 키 관리: "turnstile:" prefix로 네임스페이스 분리
     *
     * @param redisConnectionFactory Redis 연결 팩토리 (Spring Boot 자동 설정)
     * @param objectMapper           JSON 직렬화/역직렬화용 ObjectMapper
     * @return 설정된 RedisTemplate 인스턴스
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // Redis 연결 팩토리 설정 (application.yml 기반 자동 구성)
        template.setConnectionFactory(redisConnectionFactory);

        // Key 직렬화 방식: Prefix를 포함한 String (UTF-8)
        PrefixStringRedisSerializer keySerializer = new PrefixStringRedisSerializer("turnstile:");

        // Value 직렬화 방식: @class 타입 정보가 제거된 JSON (Jackson 사용)
        // 보안: 역직렬화 공격 방지를 위해 타입 정보 제거
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.deactivateDefaultTyping();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // 일반 Key-Value 직렬화 설정
        template.setKeySerializer(keySerializer);           // Key는 Prefix + String으로
        template.setValueSerializer(jsonSerializer);        // Value는 JSON으로 (@class 제외)

        // Hash 구조 직렬화 설정
        template.setHashKeySerializer(keySerializer);       // Hash Key는 Prefix + String으로
        template.setHashValueSerializer(jsonSerializer);    // Hash Value는 JSON으로 (@class 제외)

        // 설정 적용
        template.afterPropertiesSet();

        return template;
    }

    /**
     * 문자열 전용 작업을 위한 StringRedisTemplate Bean을 생성합니다.
     * <p>
     * 단순 값, 카운터, 플래그, TTL 기반 키처럼
     * 키와 값이 모두 문자열로 저장되는 경우에 사용합니다.
     *
     * @param redisConnectionFactory Redis 연결 팩토리
     * @return 설정된 StringRedisTemplate
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logRedisStartup() {
        String endpoint = resolveEndpoint();
        boolean sslEnabled = redisProperties.getSsl() != null && redisProperties.getSsl().isEnabled();
        String ping = pingRedis();
        log.info("Redis startup: endpoint={}, ssl={}, ping={}", endpoint, sslEnabled, ping);
    }

    private String resolveEndpoint() {
        String url = redisProperties.getUrl();
        if (StringUtils.hasText(url)) {
            return MaskingUtil.redisUrl(url);
        }
        String host = redisProperties.getHost();
        int port = redisProperties.getPort();
        if (StringUtils.hasText(host)) {
            return host + ":" + port;
        }
        return "unknown";
    }

    private String pingRedis() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            return pong == null ? "OK" : pong;
        } catch (Exception ex) {
            return "FAIL";
        }
    }

    /**
     * Redis Key에 자동으로 prefix를 추가하는 커스텀 Serializer.
     * <p>
     * 네임스페이스 분리를 통해 키 충돌을 방지하고 관리를 용이하게 합니다.
     */
    private static class PrefixStringRedisSerializer extends StringRedisSerializer {

        private final String prefix;

        public PrefixStringRedisSerializer(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public byte[] serialize(String key) {
            if (key == null) {
                return super.serialize(null);
            }
            // 이미 prefix가 있으면 중복 추가 방지
            String prefixedKey = key.startsWith(prefix) ? key : prefix + key;
            return super.serialize(prefixedKey);
        }

        @Override
        public String deserialize(byte[] bytes) {
            String key = super.deserialize(bytes);
            if (key != null && key.startsWith(prefix)) {
                // prefix 제거하여 반환
                return key.substring(prefix.length());
            }
            return key;
        }
    }

}
