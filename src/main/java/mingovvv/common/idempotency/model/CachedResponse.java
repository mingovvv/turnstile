package mingovvv.common.idempotency.model;

/**
 * 아이템포턴시 재응답용 캐시 객체입니다.
 *
 * @param status      HTTP 상태 코드
 * @param contentType 응답 Content-Type
 * @param body        응답 바디 원본 바이트
 */
public record CachedResponse(
    int status,
    String contentType,
    byte[] body
) {
}
