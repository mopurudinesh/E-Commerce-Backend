package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.security.UserPrincipal;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.service.PasswordRecoveryService;
import com.ecommerce.auth.service.InMemoryRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.time.Duration;

/**
 * Fixed REST API Controller strictly adhering to Master Agent Prompt Section 12 contract.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Authentication & Recovery", description = "Endpoints matching fixed API contract")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final InMemoryRateLimiter rateLimiter;

    @Value("${app.jwt.access-cookie-name:ecommerce_access_token}")
    private String accessCookieName;

    public AuthController(AuthService authService, PasswordRecoveryService passwordRecoveryService,
                          InMemoryRateLimiter rateLimiter) {
        this.authService = authService;
        this.passwordRecoveryService = passwordRecoveryService;
        this.rateLimiter = rateLimiter;
    }

    /**
     * POST /api/reg - Account registration
     */
    @PostMapping("/reg")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<UserSummaryDto>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserSummaryDto user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", user));
    }

    /**
     * POST /api/login - Authenticate with email or international phone number
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email or international phone number")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response,
            HttpServletRequest httpRequest) {

        if (!rateLimiter.allow("login:" + httpRequest.getRemoteAddr(), 10, Duration.ofMinutes(15))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many login attempts. Please try again later."));
        }

        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", authResponse));
    }

    /**
     * POST /api/logout - Revoke server session and delete cookies
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user and revoke server session")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String token = extractToken(request);
        authService.logout(token, response);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * POST /api/forgot-password - Initiate password recovery with generic message
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "Request a 6-digit recovery OTP")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {

        if (!rateLimiter.allow("recovery:" + httpRequest.getRemoteAddr(), 5, Duration.ofHours(1))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.success("If an account exists, use the configured recovery delivery method to continue."));
        }

        passwordRecoveryService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If an account exists, use the configured recovery delivery method to continue."));
    }

    /**
     * POST /api/verify-otp - Verify OTP and issue recovery authorization token
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP code")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest) {

        if (!rateLimiter.allow("otp:" + httpRequest.getRemoteAddr(), 10, Duration.ofMinutes(15))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("Too many verification attempts. Please try again later."));
        }

        String recoveryToken = passwordRecoveryService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully",
                Collections.singletonMap("recoveryToken", recoveryToken)));
    }

    /**
     * POST /api/reset-password - Reset password using recovery authorization token
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using recovery token")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordRecoveryService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully. Please log in with your new password."));
    }

    /**
     * PUT /api/change-password - Change password for authenticated session
     */
    @PutMapping("/change-password")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse response) {

        authService.changePassword(userPrincipal, request, response);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully."));
    }

    /**
     * GET /api/me - Return authenticated user's safe profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        UserSummaryDto user = authService.getCurrentUser(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success("User profile", user));
    }

    /**
     * GET /api/home - Welcome endpoint matching Master Prompt Section 12 exactly
     */
    @GetMapping("/home")
    @Operation(summary = "Welcome Home endpoint")
    public ResponseEntity<Map<String, String>> getHome() {
        return ResponseEntity.ok(Collections.singletonMap("message", "Welcome to Home Page"));
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (accessCookieName.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
