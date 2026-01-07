package mingovvv.turnstile.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 정의
 */
@Getter
public enum ErrorCode {

    // Event 관련
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다."),
    EVENT_NOT_OPEN(HttpStatus.BAD_REQUEST, "E002", "예매가 진행 중인 이벤트가 아닙니다."),

    // Queue 관련
    ALREADY_IN_QUEUE(HttpStatus.CONFLICT, "Q001", "이미 대기열에 등록되어 있습니다."),
    NOT_IN_QUEUE(HttpStatus.BAD_REQUEST, "Q002", "대기열에 등록되어 있지 않습니다."),
    QUEUE_ENTRY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Q003", "대기열 진입에 실패했습니다."),

    // Token 관련
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "T001", "입장 토큰이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "T002", "입장 토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "T003", "유효하지 않은 입장 토큰입니다."),

    // Seat 관련
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_LOCKED(HttpStatus.CONFLICT, "S002", "이미 다른 사용자가 선점한 좌석입니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.CONFLICT, "S003", "이미 예약 완료된 좌석입니다."),
    SEAT_NOT_LOCKED_BY_USER(HttpStatus.FORBIDDEN, "S004", "본인이 선점한 좌석이 아닙니다."),
    SEAT_LOCK_EXPIRED(HttpStatus.BAD_REQUEST, "S005", "좌석 선점 시간이 만료되었습니다."),

    // Payment 관련
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "P001", "결제에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P002", "결제 정보를 찾을 수 없습니다."),

    // Reservation 관련
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "예약 정보를 찾을 수 없습니다."),

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
