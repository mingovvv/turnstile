package mingovvv.common.ratelimit.impl;

import mingovvv.common.ratelimit.RateLimiter;
import mingovvv.common.ratelimit.config.RateLimitProperties;
import mingovvv.common.ratelimit.model.RateLimitResult;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryRateLimiter implements RateLimiter {

    private final RateLimitProperties properties;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(RateLimitProperties properties) {
        this.properties = properties;
    }

    /**
     * 키별 고정 윈도우 카운터 방식입니다.
     * 설정한 시간이 지나면 윈도우를 리셋합니다.
     */
    @Override
    public RateLimitResult tryConsume(String key) {
        long nowMs = System.currentTimeMillis();
        Window window = windows.computeIfAbsent(key, ignored -> new Window(nowMs));
        long windowMs = properties.getWindowSeconds() * 1000L;

        synchronized (window) {
            if (nowMs - window.windowStartMs >= windowMs) {
                window.windowStartMs = nowMs;
                window.count.set(0);
            }

            int count = window.count.incrementAndGet();
            boolean allowed = count <= properties.getMaxRequests();
            int remaining = Math.max(0, properties.getMaxRequests() - count);
            long resetEpochSeconds = (window.windowStartMs + windowMs) / 1000L;

            return new RateLimitResult(allowed, remaining, resetEpochSeconds);
        }
    }

    private static class Window {
        private volatile long windowStartMs;
        private final AtomicInteger count = new AtomicInteger(0);

        private Window(long windowStartMs) {
            this.windowStartMs = windowStartMs;
        }
    }

}
