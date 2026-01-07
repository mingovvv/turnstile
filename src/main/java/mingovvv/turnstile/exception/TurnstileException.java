package mingovvv.turnstile.exception;

import lombok.Getter;

/**
 * Turnstile 시스템 기본 예외
 */
@Getter
public class TurnstileException extends RuntimeException {

    private final ErrorCode errorCode;

    public TurnstileException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TurnstileException(ErrorCode errorCode, String additionalMessage) {
        super(errorCode.getMessage() + " - " + additionalMessage);
        this.errorCode = errorCode;
    }
}
