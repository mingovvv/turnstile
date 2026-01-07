package mingovvv.common.ratelimit;

import mingovvv.common.ratelimit.model.RateLimitResult;

public interface RateLimiter {

    /**
     * 주어진 키에 대해 1건의 요청을 소비합니다.
     * 구현체가 윈도우 전략과 잔여 횟수 계산 방식을 결정합니다.
     *
     * @param key 호출자를 구분하는 키(ip, user, client id 등)
     * @return 허용 여부 및 리셋 정보
     */
    RateLimitResult tryConsume(String key);

}
