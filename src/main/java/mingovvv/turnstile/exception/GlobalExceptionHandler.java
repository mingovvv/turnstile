package mingovvv.turnstile.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 전역 예외 처리
 */
@Slf4j
@RestControllerAdvice(basePackages = "mingovvv.turnstile")
public class GlobalExceptionHandler {

    @ExceptionHandler(TurnstileException.class)
    public ResponseEntity<Map<String, Object>> handleTurnstileException(TurnstileException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("TurnstileException: code={}, message={}", errorCode.getCode(), e.getMessage());

        Map<String, Object> body = Map.of(
                "success", false,
                "error", Map.of(
                        "code", errorCode.getCode(),
                        "message", e.getMessage()
                ),
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("Unexpected error: ", e);

        Map<String, Object> body = Map.of(
                "success", false,
                "error", Map.of(
                        "code", ErrorCode.INTERNAL_ERROR.getCode(),
                        "message", ErrorCode.INTERNAL_ERROR.getMessage()
                ),
                "timestamp", LocalDateTime.now().toString()
        );

        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus()).body(body);
    }
}
