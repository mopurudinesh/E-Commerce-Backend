package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * OTP Verification request payload matching Section 10 & 12.
 */
public class VerifyOtpRequest {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    @Pattern(regexp = "\\d{6}", message = "OTP must contain exactly 6 digits")
    private String otp;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String identifier, String otp) {
        this.identifier = identifier;
        this.otp = otp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier != null ? identifier.trim() : null;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp != null ? otp.trim() : null;
    }
}
