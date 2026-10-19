package com.ecommerce.auth.dto;

/**
 * Authentication response payload for login.
 */
public class AuthResponse {
    private Long expiresIn;
    private UserSummaryDto user;

    public AuthResponse() {
    }

    public AuthResponse(Long expiresIn, UserSummaryDto user) {
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserSummaryDto getUser() {
        return user;
    }

    public void setUser(UserSummaryDto user) {
        this.user = user;
    }
}
