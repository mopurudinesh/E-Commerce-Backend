package com.ecommerce.auth.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Default outside local development; no code is exposed without a real delivery provider. */
@Component
@Profile("!local")
public class DisabledRecoveryCodeDelivery implements RecoveryCodeDelivery {

    @Override
    public String channelName() {
        return "UNCONFIGURED";
    }

    @Override
    public void deliver(String code, LocalDateTime expiresAt) {
        // Deliberately empty. Configure a real provider before enabling recovery outside local mode.
    }
}
