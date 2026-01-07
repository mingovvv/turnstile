package mingovvv.common.idempotency.impl;

import mingovvv.common.idempotency.IdempotencyStore;
import mingovvv.common.idempotency.model.CachedResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();

    /**
     * 캐시가 존재하고 만료되지 않았으면 응답을 반환합니다.
     */
    @Override
    public Optional<CachedResponse> get(String key) {
        Entry entry = cache.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAtEpochSeconds < Instant.now().getEpochSecond()) {
            cache.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.response);
    }

    /**
     * 동일 키가 없을 때만 응답을 저장합니다.
     */
    @Override
    public boolean putIfAbsent(String key, CachedResponse response, long ttlSeconds) {
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;
        Entry entry = new Entry(response, expiresAt);
        return cache.putIfAbsent(key, entry) == null;
    }

    private record Entry(CachedResponse response, long expiresAtEpochSeconds) {
    }

}
