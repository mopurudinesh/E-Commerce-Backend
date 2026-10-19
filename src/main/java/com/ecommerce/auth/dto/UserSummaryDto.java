package com.ecommerce.auth.dto;

/**
 * Safe user summary projection matching Section 12 (GET /api/me).
 */
public class UserSummaryDto {
    private Long id;
    private String fullName;
    private String email;
    private String mobileE164;
    private String phoneRegion;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id, String fullName, String email, String mobileE164, String phoneRegion) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.mobileE164 = mobileE164;
        this.phoneRegion = phoneRegion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileE164() {
        return mobileE164;
    }

    public void setMobileE164(String mobileE164) {
        this.mobileE164 = mobileE164;
    }

    public String getPhoneRegion() {
        return phoneRegion;
    }

    public void setPhoneRegion(String phoneRegion) {
        this.phoneRegion = phoneRegion;
    }
}
