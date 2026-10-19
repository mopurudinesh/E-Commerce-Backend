package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login request payload matching Master Agent Prompt Section 12.
 * Supports email or international phone number in the 'identifier' field.
 */
public class LoginRequest {

    @NotBlank(message = "Identifier (email or international phone number) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier != null ? identifier.trim() : null;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
