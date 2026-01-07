package mingovvv.turnstile.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 좌석 선점 락 Redis Repository
 * <p>
 * SET NX EX를 활용한 원자적 좌석 선점
 * TTL: 5분 (300초)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SeatLockRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_KEY_PREFIX = "seat:lock:";
    private static final long LOCK_TTL_SECONDS = 300; // 5분

    /**
     * 좌석 선점 락 결과
     */
    public enum LockResult {
        SUCCESS,        // 선점 성공
        ALREADY_OWNED,  // 본인이 이미 선점
        LOCKED          // 다른 사용자가 선점
    }

    /**
     * 좌석 선점 시도 (Lua Script로 원자성 보장)
     */
    public LockResult tryLock(String eventId, String seatId, String userId) {
        String lockKey = lockKey(eventId, seatId);

        // Lua Script: 원자적으로 확인 및 설정
        String script = """
                local lockKey = KEYS[1]
                local userId = ARGV[1]
                local ttl = ARGV[2]

                local current = redis.call('GET', lockKey)
                if current then
                    if current == userId then
                        return 'ALREADY_OWNED'
                    else
                        return 'LOCKED'
                    end
                end

                redis.call('SET', lockKey, userId, 'EX', ttl)
                return 'SUCCESS'
                """;

        DefaultRedisScript<String> redisScript = new DefaultRedisScript<>(script, String.class);
        String result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(lockKey),
                userId,
                String.valueOf(LOCK_TTL_SECONDS)
        );

        log.debug("Seat lock attempt: eventId={}, seatId={}, userId={}, result={}", eventId, seatId, userId, result);
        return LockResult.valueOf(result);
    }

    /**
     * 좌석 선점 해제
     * 본인이 선점한 좌석만 해제 가능
     */
    public boolean unlock(String eventId, String seatId, String userId) {
        String lockKey = lockKey(eventId, seatId);

        // Lua Script: 본인이 선점한 경우에만 삭제
        String script = """
                local lockKey = KEYS[1]
                local userId = ARGV[1]

                local current = redis.call('GET', lockKey)
                if current == userId then
                    redis.call('DEL', lockKey)
                    return 1
                end
                return 0
                """;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(lockKey),
                userId
        );

        boolean unlocked = result != null && result == 1;
        log.debug("Seat unlock: eventId={}, seatId={}, userId={}, success={}", eventId, seatId, userId, unlocked);
        return unlocked;
    }

    /**
     * 좌석 선점자 조회
     */
    public Optional<String> getLockedBy(String eventId, String seatId) {
        String lockKey = lockKey(eventId, seatId);
        String userId = stringRedisTemplate.opsForValue().get(lockKey);
        return Optional.ofNullable(userId);
    }

    /**
     * 좌석이 선점되었는지 확인
     */
    public boolean isLocked(String eventId, String seatId) {
        String lockKey = lockKey(eventId, seatId);
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey));
    }

    /**
     * 특정 사용자가 선점했는지 확인
     */
    public boolean isLockedBy(String eventId, String seatId, String userId) {
        Optional<String> lockedBy = getLockedBy(eventId, seatId);
        return lockedBy.isPresent() && lockedBy.get().equals(userId);
    }

    /**
     * 남은 선점 시간 조회 (초)
     */
    public long getRemainingTtl(String eventId, String seatId) {
        String lockKey = lockKey(eventId, seatId);
        Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 선점 락 강제 삭제 (예약 확정 시 사용)
     */
    public void forceUnlock(String eventId, String seatId) {
        String lockKey = lockKey(eventId, seatId);
        stringRedisTemplate.delete(lockKey);
        log.debug("Seat force unlock: eventId={}, seatId={}", eventId, seatId);
    }

    /**
     * 선점 TTL 기본값 조회
     */
    public long getLockTtlSeconds() {
        return LOCK_TTL_SECONDS;
    }

    private String lockKey(String eventId, String seatId) {
        return LOCK_KEY_PREFIX + eventId + ":" + seatId;
    }
}
