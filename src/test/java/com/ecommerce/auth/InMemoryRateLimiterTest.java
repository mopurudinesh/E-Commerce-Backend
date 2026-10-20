package com.ecommerce.auth;

import com.ecommerce.auth.service.InMemoryRateLimiter;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateLimiterTest {
    @Test
    void stopsRequestsAfterTheConfiguredWindowLimit() {
        InMemoryRateLimiter limiter = new InMemoryRateLimiter();
        assertTrue(limiter.allow("test-key", 2, Duration.ofHours(1)));
        assertTrue(limiter.allow("test-key", 2, Duration.ofHours(1)));
        assertFalse(limiter.allow("test-key", 2, Duration.ofHours(1)));
    }
}
