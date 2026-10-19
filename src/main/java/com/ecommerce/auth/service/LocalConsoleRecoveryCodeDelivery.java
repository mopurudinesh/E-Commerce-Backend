package com.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** Local-only delivery so students can complete recovery without an email/SMS provider. */
@Component
@Profile("local")
public class LocalConsoleRecoveryCodeDelivery implements RecoveryCodeDelivery {

    private static final Logger logger = LoggerFactory.getLogger(LocalConsoleRecoveryCodeDelivery.class);

    @Override
    public String channelName() {
        return "LOCAL_CONSOLE";
    }

    @Override
    public void deliver(String code, LocalDateTime expiresAt) {
        logger.warn("LOCAL DEVELOPMENT ONLY - password recovery code: {} (expires at {})", code, expiresAt);
    }
}
