package mingovvv.common.ratelimit.impl;

import jakarta.servlet.http.HttpServletRequest;
import mingovvv.common.ratelimit.RateLimitKeyResolver;
import mingovvv.common.utils.NetworkUtil;

public class IpRateLimitKeyResolver implements RateLimitKeyResolver {

    /**
     * 클라이언트 IP(프록시 헤더 포함)를 레이트 리밋 키로 사용합니다.
     */
    @Override
    public String resolve(HttpServletRequest request) {
        return NetworkUtil.getClientIp(request);
    }

}
