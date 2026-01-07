package mingovvv.turnstile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mingovvv.turnstile.exception.ErrorCode;
import mingovvv.turnstile.exception.TurnstileException;
import mingovvv.turnstile.repository.redis.TokenRedisRepository;
import org.springframework.stereotype.Service;

/**
 * 입장 토큰 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRedisRepository tokenRepository;

    /**
     * 토큰 유효성 검증
     * 토큰이 없거나 유효하지 않으면 예외 발생
     */
    public void validateToken(String eventId, String userId, String token) {
        if (token == null || token.isBlank()) {
            throw new TurnstileException(ErrorCode.TOKEN_NOT_FOUND);
        }

        if (!tokenRepository.hasToken(eventId, userId)) {
            throw new TurnstileException(ErrorCode.TOKEN_EXPIRED);
        }

        if (!tokenRepository.isValidToken(eventId, userId, token)) {
            throw new TurnstileException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 토큰 존재 여부 확인 (예외 없이)
     */
    public boolean hasValidToken(String eventId, String userId) {
        return tokenRepository.hasToken(eventId, userId);
    }

    /**
     * 토큰 삭제
     */
    public void deleteToken(String eventId, String userId) {
        tokenRepository.deleteToken(eventId, userId);
        log.info("Token deleted: eventId={}, userId={}", eventId, userId);
    }

    /**
     * 토큰 남은 시간 조회
     */
    public long getRemainingTtl(String eventId, String userId) {
        return tokenRepository.getRemainingTtl(eventId, userId);
    }
}
