package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Password recovery challenge entity matching Master Agent Prompt Section 7.
 * Stores securely hashed OTPs, attempt counts, and recovery tokens.
 */
@Entity
@Table(name = "password_recovery_challenges")
public class PasswordRecoveryChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "otp_hash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "delivery_channel", length = 20)
    private String deliveryChannel = "UNCONFIGURED";

    @Column(name = "recovery_token_hash", length = 255)
    private String recoveryTokenHash;

    @Column(name = "recovery_expires_at")
    private LocalDateTime recoveryExpiresAt;

    public PasswordRecoveryChallenge() {
    }

    public PasswordRecoveryChallenge(User user, String otpHash, LocalDateTime expiresAt) {
        this.user = user;
        this.otpHash = otpHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.attemptCount = 0;
        this.deliveryChannel = "UNCONFIGURED";
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isConsumed() {
        return this.consumedAt != null;
    }

    public boolean isRecoveryTokenExpired() {
        return this.recoveryExpiresAt != null && LocalDateTime.now().isAfter(this.recoveryExpiresAt);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getDeliveryChannel() {
        return deliveryChannel;
    }

    public void setDeliveryChannel(String deliveryChannel) {
        this.deliveryChannel = deliveryChannel;
    }

    public String getRecoveryTokenHash() {
        return recoveryTokenHash;
    }

    public void setRecoveryTokenHash(String recoveryTokenHash) {
        this.recoveryTokenHash = recoveryTokenHash;
    }

    public LocalDateTime getRecoveryExpiresAt() {
        return recoveryExpiresAt;
    }

    public void setRecoveryExpiresAt(LocalDateTime recoveryExpiresAt) {
        this.recoveryExpiresAt = recoveryExpiresAt;
    }
}
