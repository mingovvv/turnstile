package mingovvv.turnstile.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 입장 토큰 Redis Repository
 * <p>
 * 대기열을 통과한 사용자에게 발급되는 입장 토큰 관리
 * TTL: 10분 (600초)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_KEY_PREFIX = "token:";
    private static final long TOKEN_TTL_SECONDS = 600; // 10분

    /**
     * 입장 토큰 발급
     *
     * @return 발급된 토큰
     */
    public String issueToken(String eventId, String userId) {
        String tokenKey = tokenKey(eventId, userId);
        String token = UUID.randomUUID().toString();

        stringRedisTemplate.opsForValue().set(tokenKey, token, TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        log.debug("Token issued: eventId={}, userId={}, token={}", eventId, userId, token);

        return token;
    }

    /**
     * 토큰 조회
     */
    public Optional<String> getToken(String eventId, String userId) {
        String tokenKey = tokenKey(eventId, userId);
        String token = stringRedisTemplate.opsForValue().get(tokenKey);
        return Optional.ofNullable(token);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean isValidToken(String eventId, String userId, String token) {
        Optional<String> storedToken = getToken(eventId, userId);
        return storedToken.isPresent() && storedToken.get().equals(token);
    }

    /**
     * 토큰 존재 여부 확인
     */
    public boolean hasToken(String eventId, String userId) {
        String tokenKey = tokenKey(eventId, userId);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenKey));
    }

    /**
     * 토큰 남은 TTL 조회 (초)
     */
    public long getRemainingTtl(String eventId, String userId) {
        String tokenKey = tokenKey(eventId, userId);
        Long ttl = stringRedisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 토큰 삭제
     */
    public boolean deleteToken(String eventId, String userId) {
        String tokenKey = tokenKey(eventId, userId);
        return Boolean.TRUE.equals(stringRedisTemplate.delete(tokenKey));
    }

    /**
     * 토큰 TTL 연장
     */
    public boolean extendToken(String eventId, String userId, long additionalSeconds) {
        String tokenKey = tokenKey(eventId, userId);
        Long currentTtl = stringRedisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);

        if (currentTtl == null || currentTtl <= 0) {
            return false;
        }

        return Boolean.TRUE.equals(
                stringRedisTemplate.expire(tokenKey, currentTtl + additionalSeconds, TimeUnit.SECONDS)
        );
    }

    /**
     * 특정 이벤트의 현재 토큰 보유자 수 조회
     * SCAN 명령어를 사용하여 패턴 매칭
     */
    public long countByEventId(String eventId) {
        String pattern = TOKEN_KEY_PREFIX + eventId + ":*";
        long count = 0;

        var cursor = stringRedisTemplate.scan(
                org.springframework.data.redis.core.ScanOptions.scanOptions()
                        .match(pattern)
                        .count(100)
                        .build()
        );

        try {
            while (cursor.hasNext()) {
                cursor.next();
                count++;
            }
        } finally {
            cursor.close();
        }

        return count;
    }

    private String tokenKey(String eventId, String userId) {
        return TOKEN_KEY_PREFIX + eventId + ":" + userId;
    }
}
