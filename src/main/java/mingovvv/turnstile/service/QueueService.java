package mingovvv.turnstile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.domain.Event;
import mingovvv.turnstile.dto.response.QueueStatusResponse;
import mingovvv.turnstile.exception.ErrorCode;
import mingovvv.turnstile.exception.TurnstileException;
import mingovvv.turnstile.repository.redis.QueueRedisRepository;
import mingovvv.turnstile.repository.redis.TokenRedisRepository;
import mingovvv.turnstile.sse.QueueSseEmitterRegistry;
import mingovvv.turnstile.sse.QueueSseEvent;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * 대기열 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final EventService eventService;
    private final QueueRedisRepository queueRepository;
    private final TokenRedisRepository tokenRepository;
    private final QueueSseEmitterRegistry sseRegistry;

    // 평균 처리 시간 (초) - 순번당 예상 대기 시간 계산용
    private static final int AVG_PROCESSING_TIME_PER_USER = 3;

    /**
     * 대기열 진입
     */
    public QueueStatusResponse enterQueue(String eventId, String userId) {
        // 이벤트 유효성 검증
        Event event = eventService.findEventOrThrow(eventId);
        eventService.validateEventOpen(eventId);

        // 이미 토큰이 있는지 확인 (이미 입장한 사용자)
        if (tokenRepository.hasToken(eventId, userId)) {
            String token = tokenRepository.getToken(eventId, userId).orElse(null);
            log.info("User already has token: eventId={}, userId={}", eventId, userId);
            return QueueStatusResponse.canEnter(eventId, userId, token);
        }

        // 이미 대기열에 있는지 확인
        if (queueRepository.isInQueue(eventId, userId)) {
            throw new TurnstileException(ErrorCode.ALREADY_IN_QUEUE, userId);
        }

        // 대기열 진입
        long sequence = queueRepository.enter(eventId, userId);
        log.info("User entered queue: eventId={}, userId={}, sequence={}", eventId, userId, sequence);

        // 현재 상태 조회 및 반환
        return getQueueStatus(eventId, userId);
    }

    /**
     * 대기열 상태 조회
     */
    public QueueStatusResponse getQueueStatus(String eventId, String userId) {
        // 이미 토큰이 있는지 확인
        Optional<String> token = tokenRepository.getToken(eventId, userId);
        if (token.isPresent()) {
            return QueueStatusResponse.canEnter(eventId, userId, token.get());
        }

        // 대기열 순번 조회
        Long position = queueRepository.getPosition(eventId, userId);
        if (position == null) {
            return QueueStatusResponse.notInQueue(eventId, userId);
        }

        long totalWaiting = queueRepository.getTotalWaiting(eventId);
        int estimatedWaitSeconds = (int) (position * AVG_PROCESSING_TIME_PER_USER);

        return QueueStatusResponse.waiting(eventId, userId, position, totalWaiting, estimatedWaitSeconds);
    }

    /**
     * 대기열 이탈
     */
    public void leaveQueue(String eventId, String userId) {
        boolean removed = queueRepository.leave(eventId, userId);
        if (!removed) {
            throw new TurnstileException(ErrorCode.NOT_IN_QUEUE, userId);
        }
        log.info("User left queue: eventId={}, userId={}", eventId, userId);
    }

    /**
     * 대기열에서 N명 입장 처리 (Scheduler에서 호출)
     *
     * @param count 입장 처리할 인원 수
     * @return 입장 처리된 사용자 수
     */
    public int processQueue(String eventId, int count) {
        Set<String> users = queueRepository.popFront(eventId, count);

        if (users == null || users.isEmpty()) {
            return 0;
        }

        // 각 사용자에게 토큰 발급 및 SSE 알림
        for (String userId : users) {
            String token = tokenRepository.issueToken(eventId, userId);
            log.info("Token issued: eventId={}, userId={}, token={}", eventId, userId, token.substring(0, 8) + "...");

            // SSE로 토큰 발급 알림
            QueueSseEvent sseEvent = QueueSseEvent.tokenIssued(eventId, userId, token);
            sseRegistry.send(eventId, userId, sseEvent);
        }

        // 대기열에 남아있는 사용자들에게 순번 업데이트 알림
        broadcastQueueUpdate(eventId);

        return users.size();
    }

    /**
     * 대기열 사용자들에게 순번 업데이트 SSE 전송
     */
    private void broadcastQueueUpdate(String eventId) {
        long totalWaiting = queueRepository.getTotalWaiting(eventId);

        // 현재 대기열의 모든 사용자에게 업데이트 (상위 100명만)
        Set<String> waitingUsers = queueRepository.getTopUsers(eventId, 100);

        if (waitingUsers == null) {
            return;
        }

        for (String userId : waitingUsers) {
            Long position = queueRepository.getPosition(eventId, userId);
            if (position != null) {
                int estimatedWait = (int) (position * AVG_PROCESSING_TIME_PER_USER);
                QueueSseEvent event = QueueSseEvent.queueUpdate(eventId, userId, position, totalWaiting, estimatedWait);
                sseRegistry.send(eventId, userId, event);
            }
        }
    }

    /**
     * 대기열 총 인원 조회
     */
    public long getTotalWaiting(String eventId) {
        return queueRepository.getTotalWaiting(eventId);
    }
}
