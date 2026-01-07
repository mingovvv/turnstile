package mingovvv.turnstile.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * 대기열 Redis Repository (Sorted Set 활용)
 * <p>
 * Score = timestamp(ms) × 1,000,000 + sequence
 * Member = userId
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String QUEUE_KEY_PREFIX = "queue:";
    private static final String SEQUENCE_KEY_PREFIX = "queue:sequence:";

    /**
     * 대기열 진입
     * Score = timestamp × 1,000,000 + sequence (동일 시간 진입 시 순서 보장)
     *
     * @return 할당된 순번 (sequence)
     */
    public long enter(String eventId, String userId) {
        String queueKey = queueKey(eventId);
        String sequenceKey = sequenceKey(eventId);

        // 원자적으로 sequence 증가
        Long sequence = stringRedisTemplate.opsForValue().increment(sequenceKey);
        if (sequence == null) {
            sequence = 1L;
        }

        // Score 계산: timestamp × 1,000,000 + sequence
        long timestamp = System.currentTimeMillis();
        double score = timestamp * 1_000_000.0 + sequence;

        stringRedisTemplate.opsForZSet().add(queueKey, userId, score);
        log.debug("Queue enter: eventId={}, userId={}, sequence={}, score={}", eventId, userId, sequence, score);

        return sequence;
    }

    /**
     * 대기열 이탈
     */
    public boolean leave(String eventId, String userId) {
        String queueKey = queueKey(eventId);
        Long removed = stringRedisTemplate.opsForZSet().remove(queueKey, userId);
        return removed != null && removed > 0;
    }

    /**
     * 대기열 순번 조회 (0-based)
     *
     * @return 순번 (null이면 대기열에 없음)
     */
    public Long getPosition(String eventId, String userId) {
        String queueKey = queueKey(eventId);
        return stringRedisTemplate.opsForZSet().rank(queueKey, userId);
    }

    /**
     * 대기열에 있는지 확인
     */
    public boolean isInQueue(String eventId, String userId) {
        return getPosition(eventId, userId) != null;
    }

    /**
     * 대기열에서 상위 N명 꺼내기 (입장 처리용)
     * ZPOPMIN과 동일한 효과
     */
    public Set<String> popFront(String eventId, int count) {
        String queueKey = queueKey(eventId);
        ZSetOperations<String, String> zSetOps = stringRedisTemplate.opsForZSet();

        // 상위 N명 조회
        Set<String> users = zSetOps.range(queueKey, 0, count - 1);

        if (users != null && !users.isEmpty()) {
            // 조회한 사용자들 제거
            zSetOps.remove(queueKey, users.toArray());
            log.debug("Queue pop: eventId={}, count={}, users={}", eventId, users.size(), users);
        }

        return users;
    }

    /**
     * 전체 대기 인원 조회
     */
    public long getTotalWaiting(String eventId) {
        String queueKey = queueKey(eventId);
        Long size = stringRedisTemplate.opsForZSet().zCard(queueKey);
        return size != null ? size : 0;
    }

    /**
     * Score 조회 (디버깅용)
     */
    public Double getScore(String eventId, String userId) {
        String queueKey = queueKey(eventId);
        return stringRedisTemplate.opsForZSet().score(queueKey, userId);
    }

    /**
     * 대기열 상위 N명 조회 (제거하지 않음, SSE 브로드캐스트용)
     */
    public Set<String> getTopUsers(String eventId, int count) {
        String queueKey = queueKey(eventId);
        return stringRedisTemplate.opsForZSet().range(queueKey, 0, count - 1);
    }

    /**
     * 대기열 초기화
     */
    public void clear(String eventId) {
        stringRedisTemplate.delete(queueKey(eventId));
        stringRedisTemplate.delete(sequenceKey(eventId));
    }

    private String queueKey(String eventId) {
        return QUEUE_KEY_PREFIX + eventId;
    }

    private String sequenceKey(String eventId) {
        return SEQUENCE_KEY_PREFIX + eventId;
    }
}
