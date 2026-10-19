package com.ecommerce.auth.service;

import java.time.LocalDateTime;

/** Delivers a recovery code through the configured channel. */
public interface RecoveryCodeDelivery {

    String channelName();

    void deliver(String code, LocalDateTime expiresAt);
}
