package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.entity.UserSession;
import com.ecommerce.auth.exception.BadRequestException;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.repository.UserSessionRepository;
import com.ecommerce.auth.security.JwtTokenProvider;
import com.ecommerce.auth.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Authentication and session service implementing Master Agent Prompt requirements.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PhoneValidationService phoneValidationService;

    @Value("${app.jwt.access-cookie-name:ecommerce_access_token}")
    private String accessCookieName;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    public AuthService(UserRepository userRepository,
                       UserSessionRepository userSessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       PhoneValidationService phoneValidationService) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.phoneValidationService = phoneValidationService;
    }

    /**
     * Registers a new customer account (POST /api/reg).
     */
    @Transactional
    public UserSummaryDto register(RegisterRequest request) {
        // 1. Password confirmation check
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Password and confirm password do not match.");
        }

        // 2. Email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email address already exists.");
        }

        // 3. International phone validation & E.164 normalization
        String region = request.getPhoneRegion() != null ? request.getPhoneRegion() : "IN";
        if (!phoneValidationService.isValid(request.getMobileNumber(), region)) {
            throw new BadRequestException("Please enter a valid international mobile number.");
        }
        String mobileE164 = phoneValidationService.formatToE164(request.getMobileNumber(), region);

        // 4. Mobile uniqueness
        if (userRepository.existsByMobileE164(mobileE164)) {
            throw new BadRequestException("An account with this mobile number already exists.");
        }

        // 5. Create user with BCrypt hashed password
        User user = new User(
                request.getFullName(),
                request.getEmail(),
                mobileE164,
                region,
                passwordEncoder.encode(request.getPassword())
        );

        User saved = userRepository.save(user);
        return new UserSummaryDto(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getMobileE164(), saved.getPhoneRegion());
    }

    /**
     * Authenticates user using email or international phone (POST /api/login).
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        String identifier = request.getIdentifier().trim();

        // If identifier looks like a phone number, normalize to E.164 for exact lookup
        String lookupIdentifier = identifier;
        if (identifier.startsWith("+") || identifier.matches("^[0-9]+$")) {
            lookupIdentifier = phoneValidationService.formatToE164(identifier, null);
        }

        User user = userRepository.findByEmailOrMobileE164(identifier, lookupIdentifier)
                .orElseThrow(() -> new BadCredentialsException("Invalid identifier or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid identifier or password.");
        }

        // Generate unique session jti
        String jti = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusMillis(tokenProvider.getTokenExpirationMs());

        UserSession session = new UserSession(user, jti, expiresAt);
        userSessionRepository.save(session);

        UserPrincipal principal = UserPrincipal.create(user);
        String jwtToken = tokenProvider.generateToken(principal, jti);

        // Set HttpOnly SameSite cookie
        attachCookie(response, jwtToken);

        UserSummaryDto userSummary = new UserSummaryDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileE164(),
                user.getPhoneRegion()
        );

        return new AuthResponse(tokenProvider.getTokenExpirationMs() / 1000, userSummary);
    }

    /**
     * Revokes active session and purges session cookies (POST /api/logout).
     */
    @Transactional
    public void logout(String token, HttpServletResponse response) {
        if (token != null && tokenProvider.validateToken(token)) {
            try {
                String jti = tokenProvider.getJtiFromToken(token);
                userSessionRepository.findByJti(jti).ifPresent(session -> {
                    session.setRevokedAt(Instant.now());
                    userSessionRepository.save(session);
                });
            } catch (Exception ignored) {
            }
        }

        clearAccessCookie(response);
        SecurityContextHolder.clearContext();
    }

    /**
     * Authenticated password change (PUT /api/change-password).
     */
    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request,
                               HttpServletResponse response) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match.");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User account not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match our records.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // Revoke all active sessions for this user
        List<UserSession> activeSessions = userSessionRepository.findAllByUserAndRevokedAtIsNull(user);
        for (UserSession s : activeSessions) {
            s.setRevokedAt(Instant.now());
        }
        userSessionRepository.saveAll(activeSessions);
        clearAccessCookie(response);
    }

    public UserSummaryDto getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        return new UserSummaryDto(user.getId(), user.getFullName(), user.getEmail(), user.getMobileE164(), user.getPhoneRegion());
    }

    private void attachCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(accessCookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(tokenProvider.getTokenExpirationMs() / 1000)
                .sameSite(cookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearAccessCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from(accessCookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    }
}
