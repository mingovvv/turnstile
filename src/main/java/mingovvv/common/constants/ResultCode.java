package mingovvv.common.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

public class ResultCode {

    @Getter
    @RequiredArgsConstructor
    public enum Success implements ResultType {

        OK("SUC-000", "Success.", HttpStatus.OK),
        CREATED("SUC-001", "Resource created successfully.", HttpStatus.CREATED),
        NO_CONTENT("SUC-002", "Processed successfully, no content.", HttpStatus.NO_CONTENT);

        private final String code;
        private final String message;
        private final HttpStatus httpStatus;

    }

    @Getter
    @RequiredArgsConstructor
    public enum Error implements ResultType {

        // AUTH: 인증 (Authentication) 오류
        AUTH_REQUIRED("AUTH-001", "Authentication is required.", HttpStatus.UNAUTHORIZED), // 401
        AUTH_TOKEN_EXPIRED("AUTH-002", "Token has expired.", HttpStatus.UNAUTHORIZED), // 401
        AUTH_TOKEN_INVALID("AUTH-003", "Invalid token.", HttpStatus.UNAUTHORIZED), // 401
        AUTH_LOGIN_FAILED("AUTH-004", "Login failed. Check your ID or password.", HttpStatus.UNAUTHORIZED), // 401

        // OAUTH: OAuth 2.0 관련 오류
        OAUTH_UNSUPPORTED_GRANT_TYPE("OAUTH-001", "Unsupported grant_type.", HttpStatus.BAD_REQUEST), // 400
        OAUTH_INVALID_CLIENT("OAUTH-002", "Invalid client_id or client_secret.", HttpStatus.UNAUTHORIZED), // 401
        OAUTH_CLIENT_DISABLED("OAUTH-003", "Client is disabled.", HttpStatus.FORBIDDEN), // 403
        OAUTH_INVALID_SCOPE("OAUTH-004", "Invalid scope.", HttpStatus.BAD_REQUEST), // 400

        // ACCESS: 인가 (Authorization)
        ACCESS_DENIED("ACCESS-001", "Access denied.", HttpStatus.FORBIDDEN), // 403
        ACCESS_IP_NOT_ALLOWED("ACCESS-002", "Access denied from this IP address.", HttpStatus.FORBIDDEN), // 403

        // REQ: 클라이언트 요청 오류
        REQ_INVALID("REQ-001", "Invalid request.", HttpStatus.BAD_REQUEST), // 400
        REQ_MISSING_PARAMETER("REQ-002", "Missing parameter.", HttpStatus.BAD_REQUEST), // 400
        REQ_INVALID_PARAMETER("REQ-003", "Invalid parameter.", HttpStatus.BAD_REQUEST), // 400
        REQ_INVALID_FORMAT("REQ-004", "Invalid format.", HttpStatus.BAD_REQUEST), // 400
        REQ_METHOD_NOT_ALLOWED("REQ-006", "Method not allowed.", HttpStatus.METHOD_NOT_ALLOWED), // 405
        REQ_UNSUPPORTED_MEDIA_TYPE("REQ-007", "Unsupported media type.", HttpStatus.UNSUPPORTED_MEDIA_TYPE), // 415 (ex: json 기대했는데 text 보냄)
        REQ_RATE_LIMITED("REQ-008", "Too many requests.", HttpStatus.TOO_MANY_REQUESTS), // 429

        // RSC: 리소스 오류
        RSC_NOT_FOUND("RSC-001", "Resource not found.", HttpStatus.NOT_FOUND), // 404
        RSC_ALREADY_EXISTS("RSC-003", "Resource already exists.", HttpStatus.CONFLICT), // 409 (중복 생성 시)

        // BIZ: 비즈니스 로직 오류
        BIZ_RULE_VIOLATION("BIZ-001", "Business rule violation.", HttpStatus.BAD_REQUEST),

        // API: 외부 연동
        API_CALL_FAILED("API-001", "External API call failed.", HttpStatus.INTERNAL_SERVER_ERROR),
        API_TIMEOUT("API-002", "External API timeout.", HttpStatus.GATEWAY_TIMEOUT), // 504
        API_SERVICE_UNAVAILABLE("API-003", "External service unavailable.", HttpStatus.SERVICE_UNAVAILABLE), // 503

        // DB : 데이터베이스 오류
        DB_ERROR("DB-001", "Database error.", HttpStatus.INTERNAL_SERVER_ERROR), // 500

        // FILE: 파일 오류
        FILE_SIZE_EXCEEDED("FILE-003", "File size exceeded maximum limit.", HttpStatus.PAYLOAD_TOO_LARGE), // 413
        FILE_EXTENSION_NOT_ALLOWED("FILE-004", "File extension not allowed.", HttpStatus.BAD_REQUEST), // 400

        // SEC
        SEC_ENCRYPTION_ERROR("SEC-001", "Encryption failed.", HttpStatus.INTERNAL_SERVER_ERROR), // 500

        // SYS: 시스템 오류
        SYS_INTERNAL_ERROR("SYS-001", "Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR), // 500
        SYS_MAINTENANCE("SYS-002", "System maintenance.", HttpStatus.SERVICE_UNAVAILABLE); // 503

        private final String code;
        private final String message;
        private final HttpStatus httpStatus;

    }

    public static boolean isSuccess(String code) {
        return Arrays.stream(Success.values())
                .anyMatch(success -> success.getCode().equals(code));
    }

}
