package mingovvv.common.ratelimit.model;

/**
 * 레이트 리밋 체크 결과입니다.
 *
 * @param allowed           요청 진행 가능 여부
 * @param remaining         현재 윈도우의 남은 횟수
 * @param resetEpochSeconds 윈도우가 리셋되는 epoch seconds
 */
public record RateLimitResult(
    boolean allowed,
    int remaining,
    long resetEpochSeconds
) {
}
