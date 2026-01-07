package mingovvv.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;

public interface RateLimitKeyResolver {

    /**
     * 요청에서 레이트 리밋 키를 추출합니다.
     * 호출자 기준으로 안정적인 값(ip, user id, client id 등)을 반환해야 합니다.
     *
     * @param request 현재 HTTP 요청
     * @return 레이트 리밋 키 (추출 불가 시 null)
     */
    String resolve(HttpServletRequest request);

}
