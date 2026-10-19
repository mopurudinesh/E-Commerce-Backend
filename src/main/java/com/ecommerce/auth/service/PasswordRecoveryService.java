package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.ForgotPasswordRequest;
import com.ecommerce.auth.dto.ResetPasswordRequest;
import com.ecommerce.auth.dto.VerifyOtpRequest;
import com.ecommerce.auth.entity.PasswordRecoveryChallenge;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.entity.UserSession;
import com.ecommerce.auth.exception.BadRequestException;
import com.ecommerce.auth.repository.PasswordRecoveryChallengeRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.repository.UserSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service managing secure password recovery challenges and OTP verification matching Section 14.
 */
@Service
public class PasswordRecoveryService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RECOVERY_TOKEN_EXPIRY_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordRecoveryChallengeRepository challengeRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final PhoneValidationService phoneValidationService;
    private final RecoveryCodeDelivery recoveryCodeDelivery;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordRecoveryService(UserRepository userRepository,
                                   PasswordRecoveryChallengeRepository challengeRepository,
                                   UserSessionRepository userSessionRepository,
                                   PasswordEncoder passwordEncoder,
                                   PhoneValidationService phoneValidationService,
                                   RecoveryCodeDelivery recoveryCodeDelivery) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.phoneValidationService = phoneValidationService;
        this.recoveryCodeDelivery = recoveryCodeDelivery;
    }

    /**
     * Initiates recovery challenge. Generic response protects against user enumeration.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String identifier = request.getIdentifier().trim();
        String lookup = identifier;
        if (identifier.startsWith("+") || identifier.matches("^[0-9]+$")) {
            lookup = phoneValidationService.formatToE164(identifier, null);
        }

        userRepository.findByEmailOrMobileE164(identifier, lookup).ifPresent(user -> {
            Optional<PasswordRecoveryChallenge> previousChallenge =
                    challengeRepository.findTopByUserOrderByCreatedAtDesc(user);
            if (previousChallenge.isPresent()
                    && Duration.between(previousChallenge.get().getCreatedAt(), LocalDateTime.now()).getSeconds() < 60) {
                return; // Keep the public response generic while enforcing the resend cooldown.
            }

            // Delete previous challenges for this user
            challengeRepository.deleteByUser(user);

            // Generate 6-digit OTP
            String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

            PasswordRecoveryChallenge challenge = new PasswordRecoveryChallenge(
                    user,
                    passwordEncoder.encode(otp),
                    expiresAt
            );
            challenge.setDeliveryChannel(recoveryCodeDelivery.channelName());
            challengeRepository.save(challenge);
            recoveryCodeDelivery.deliver(otp, expiresAt);
        });
    }

    /**
     * Verifies OTP and returns single-use recovery authorization token.
     */
    @Transactional(noRollbackFor = BadRequestException.class)
    public String verifyOtp(VerifyOtpRequest request) {
        String identifier = request.getIdentifier().trim();
        String lookup = identifier;
        if (identifier.startsWith("+") || identifier.matches("^[0-9]+$")) {
            lookup = phoneValidationService.formatToE164(identifier, null);
        }

        User user = userRepository.findByEmailOrMobileE164(identifier, lookup)
                .orElseThrow(() -> new BadRequestException("Invalid recovery request."));

        PasswordRecoveryChallenge challenge = challengeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("No active recovery request found."));

        if (challenge.isExpired()) {
            throw new BadRequestException("OTP has expired. Please request a new recovery code.");
        }

        if (challenge.isConsumed()) {
            throw new BadRequestException("This recovery code has already been used. Please request a new code.");
        }

        if (challenge.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new BadRequestException("Maximum verification attempts exceeded. Please request a new code.");
        }

        // Increment attempt count
        challenge.setAttemptCount(challenge.getAttemptCount() + 1);

        if (!passwordEncoder.matches(request.getOtp(), challenge.getOtpHash())) {
            challengeRepository.save(challenge);
            throw new BadRequestException("Invalid OTP code. Remaining attempts: " + (MAX_ATTEMPTS - challenge.getAttemptCount()));
        }

        // Generate short-lived recovery token
        String rawRecoveryToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String hashedToken = hashSha256(rawRecoveryToken);

        challenge.setConsumedAt(LocalDateTime.now());
        challenge.setRecoveryTokenHash(hashedToken);
        challenge.setRecoveryExpiresAt(LocalDateTime.now().plusMinutes(RECOVERY_TOKEN_EXPIRY_MINUTES));
        challengeRepository.save(challenge);

        return rawRecoveryToken;
    }

    /**
     * Resets password using single-use recovery authorization token.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match.");
        }

        String hashedToken = hashSha256(request.getRecoveryToken());
        PasswordRecoveryChallenge challenge = challengeRepository.findByRecoveryTokenHash(hashedToken)
                .orElseThrow(() -> new BadRequestException("Invalid or expired recovery authorization."));

        if (challenge.isRecoveryTokenExpired()) {
            throw new BadRequestException("Recovery authorization has expired. Please restart recovery.");
        }

        User user = challenge.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Invalidate challenge
        challengeRepository.delete(challenge);

        // Revoke all active sessions for this user (Master Prompt Section 12 & 14)
        List<UserSession> activeSessions = userSessionRepository.findAllByUserAndRevokedAtIsNull(user);
        for (UserSession s : activeSessions) {
            s.setRevokedAt(Instant.now());
        }
        userSessionRepository.saveAll(activeSessions);
    }

    private String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
