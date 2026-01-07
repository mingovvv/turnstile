package mingovvv.common.idempotency;

import mingovvv.common.idempotency.model.CachedResponse;

import java.util.Optional;

public interface IdempotencyStore {

    /**
     * 주어진 키에 대한 캐시된 응답을 조회합니다.
     *
     * @param key 아이템포턴시 키(일반적으로 헤더 + 호출자 스코프)
     * @return 캐시가 존재하고 만료되지 않았으면 응답 반환
     */
    Optional<CachedResponse> get(String key);

    /**
     * 동일 키가 없을 때만 응답을 저장합니다.
     *
     * @param key 아이템포턴시 키
     * @param response 저장할 응답
     * @param ttlSeconds TTL(초)
     * @return 저장 성공 시 true, 이미 존재하면 false
     */
    boolean putIfAbsent(String key, CachedResponse response, long ttlSeconds);

}
