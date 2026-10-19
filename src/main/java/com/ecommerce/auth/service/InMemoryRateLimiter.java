package com.ecommerce.auth.service;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** A small per-process limiter for local development and a single backend instance. */
@Component
public class InMemoryRateLimiter {
    private record Window(long startedAt, int requests) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String key, int maximumRequests, Duration windowDuration) {
        long now = System.currentTimeMillis();
        long durationMillis = windowDuration.toMillis();
        AtomicBoolean allowed = new AtomicBoolean(true);

        windows.compute(key, (ignored, window) -> {
            if (window == null || now - window.startedAt() >= durationMillis) {
                return new Window(now, 1);
            }
            if (window.requests() >= maximumRequests) {
                allowed.set(false);
                return window;
            }
            return new Window(window.startedAt(), window.requests() + 1);
        });

        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= durationMillis);
        }
        return allowed.get();
    }
}
