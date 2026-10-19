package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Forgot password request payload matching Section 10 & 12.
 */
public class ForgotPasswordRequest {

    @NotBlank(message = "Identifier (email or international phone number) is required")
    private String identifier;

    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier != null ? identifier.trim() : null;
    }
}
