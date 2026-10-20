package com.ecommerce.auth;

import com.ecommerce.auth.entity.PasswordRecoveryChallenge;
import com.ecommerce.auth.entity.UserSession;
import com.ecommerce.auth.repository.PasswordRecoveryChallengeRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.repository.UserSessionRepository;
import com.ecommerce.auth.service.RecoveryCodeDelivery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.filter.CorsFilter;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthIntegrationTest.RecoveryDeliveryTestConfig.class)
class AuthIntegrationTest {
    private static final String EMAIL = "integration@example.test";
    private static final String PHONE = "+14155552671";
    private static final String PASSWORD = "Example@123";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired UserSessionRepository sessions;
    @Autowired PasswordRecoveryChallengeRepository challenges;
    @Autowired CapturingRecoveryDelivery recoveryDelivery;

    @BeforeEach
    void clearDatabase() {
        sessions.deleteAll();
        challenges.deleteAll();
        users.deleteAll();
        recoveryDelivery.code = null;
    }

    @Test
    void registrationNormalizesInternationalNumberPersistsHashedPasswordAndRejectsDuplicates() throws Exception {
        register(EMAIL, PHONE).andExpect(status().isCreated());
        var stored = users.findByEmail(EMAIL).orElseThrow();
        assertEquals(PHONE, stored.getMobileE164());
        assertNotEquals(PASSWORD, stored.getPasswordHash());
        register("international@example.test", "+447911123456", PASSWORD)
                .andExpect(status().isCreated());
        assertEquals("+447911123456", users.findByEmail("international@example.test").orElseThrow().getMobileE164());

        register("integration@example.test", "+447911123456").andExpect(status().isBadRequest());
        register("second@example.test", PHONE).andExpect(status().isBadRequest());
        register("third@example.test", "+123").andExpect(status().isBadRequest());
        register("bad-confirm@example.test", "+447911123456", "Different@123")
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginByEmailAndPhoneCreatesHttpOnlyCookieAndProtectedSessionCanLogout() throws Exception {
        createUser();
        MvcResult emailLogin = login(EMAIL, PASSWORD).andExpect(status().isOk())
                .andExpect(cookie().httpOnly("ecommerce_access_token", true)).andReturn();
        var cookie = emailLogin.getResponse().getCookie("ecommerce_access_token");
        assertNotNull(cookie);
        mvc.perform(get("/api/home").cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Welcome to Home Page"));
        mvc.perform(get("/api/me").cookie(cookie)).andExpect(status().isOk());

        mvc.perform(post("/api/logout").cookie(cookie).with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/home").cookie(cookie)).andExpect(status().isUnauthorized());

        login(PHONE, PASSWORD).andExpect(status().isOk());
        var expiringCookie = login(EMAIL, PASSWORD).andReturn().getResponse().getCookie("ecommerce_access_token");
        var active = sessions.findAllByUserAndRevokedAtIsNull(users.findByEmail(EMAIL).orElseThrow());
        active.forEach(session -> session.setExpiresAt(java.time.Instant.now().minusSeconds(1)));
        sessions.saveAll(active);
        mvc.perform(get("/api/home").cookie(expiringCookie)).andExpect(status().isUnauthorized());
        login(EMAIL, "Wrong@123").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid username, email, or password")));
        login("missing@example.test", "Wrong@123").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("Invalid username, email, or password")));
    }

    @Test
    void csrfIsRequiredForMutationsAndCorsAllowsOnlyConfiguredOrigin() throws Exception {
        mvc.perform(post("/api/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"unknown@example.test\"}"))
                .andExpect(status().isForbidden());
        MvcResult csrf = mvc.perform(get("/api/csrf")).andExpect(status().isOk())
                .andExpect(jsonPath("$.csrfToken").isNotEmpty()).andReturn();
        var csrfCookie = csrf.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(csrfCookie);
        mvc.perform(post("/api/forgot-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"unknown@example.test\"}"))
                .andExpect(status().isOk());
        mvc.perform(options("/api/login").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-xsrf-token"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
        mvc.perform(options("/api/login").header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void recoveryUsesGenericResponseExpiresLimitsAttemptsConsumesCodeAndRevokesSessions() throws Exception {
        createUser();
        var cookie = login(EMAIL, PASSWORD).andReturn().getResponse().getCookie("ecommerce_access_token");
        mvc.perform(post("/api/forgot-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"" + PHONE + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message", containsString("If an account exists")));
        String otp = recoveryDelivery.code;
        assertNotNull(otp);
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/verify-otp").with(SecurityMockMvcRequestPostProcessors.csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"identifier\":\"" + PHONE + "\",\"otp\":\"000000\"}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/verify-otp").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + PHONE + "\",\"otp\":\"" + otp + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("Maximum")));

        // A new challenge can be verified once, then its recovery token can reset only once.
        challenges.deleteAll();
        mvc.perform(post("/api/forgot-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"" + PHONE + "\"}"));
        otp = recoveryDelivery.code;
        String token = mvc.perform(post("/api/verify-otp").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + PHONE + "\",\"otp\":\"" + otp + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String recoveryToken = new com.fasterxml.jackson.databind.ObjectMapper().readTree(token)
                .path("data").path("recoveryToken").asText();
        mvc.perform(post("/api/reset-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recoveryToken\":\"" + recoveryToken + "\",\"newPassword\":\"NewPass@456\",\"confirmPassword\":\"NewPass@456\"}"))
                .andExpect(status().isOk());
        assertTrue(sessions.findAllByUserAndRevokedAtIsNull(users.findByEmail(EMAIL).orElseThrow()).isEmpty());
        mvc.perform(get("/api/home").cookie(cookie)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/reset-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recoveryToken\":\"" + recoveryToken + "\",\"newPassword\":\"NewPass@456\",\"confirmPassword\":\"NewPass@456\"}"))
                .andExpect(status().isBadRequest());
        login(EMAIL, "NewPass@456").andExpect(status().isOk());
    }

    @Test
    void changePasswordRequiresAuthenticationValidatesCurrentPasswordAndRevokesAllSessions() throws Exception {
        createUser();
        mvc.perform(put("/api/change-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(changeBody(PASSWORD, "Changed@123")))
                .andExpect(status().isUnauthorized());
        var first = login(EMAIL, PASSWORD).andReturn().getResponse().getCookie("ecommerce_access_token");
        var second = login(PHONE, PASSWORD).andReturn().getResponse().getCookie("ecommerce_access_token");
        mvc.perform(put("/api/change-password").cookie(first).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(changeBody("Wrong@123", "Changed@123")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/change-password").cookie(first).with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(changeBody(PASSWORD, "Changed@123")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/home").cookie(first)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/home").cookie(second)).andExpect(status().isUnauthorized());
        login(EMAIL, "Changed@123").andExpect(status().isOk());
    }

    @Test
    void expiredOtpAndExpiredRecoveryAuthorizationAreRejected() throws Exception {
        createUser();
        mvc.perform(post("/api/forgot-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"" + EMAIL + "\"}"));
        PasswordRecoveryChallenge challenge = challenges.findAll().getFirst();
        challenge.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        challenges.save(challenge);
        mvc.perform(post("/api/verify-otp").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + EMAIL + "\",\"otp\":\"" + recoveryDelivery.code + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("expired")));

        challenges.deleteAll();
        mvc.perform(post("/api/forgot-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"identifier\":\"" + EMAIL + "\"}"));
        String result = mvc.perform(post("/api/verify-otp").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"" + EMAIL + "\",\"otp\":\"" + recoveryDelivery.code + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String recoveryToken = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result)
                .path("data").path("recoveryToken").asText();
        challenge = challenges.findAll().getFirst();
        challenge.setRecoveryExpiresAt(LocalDateTime.now().minusMinutes(1));
        challenges.save(challenge);
        mvc.perform(post("/api/reset-password").with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recoveryToken\":\"" + recoveryToken + "\",\"newPassword\":\"NewPass@456\",\"confirmPassword\":\"NewPass@456\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message", containsString("expired")));
    }

    private void createUser() throws Exception { register(EMAIL, PHONE).andExpect(status().isCreated()); }
    private org.springframework.test.web.servlet.ResultActions register(String email, String phone) throws Exception {
        return register(email, phone, PASSWORD);
    }
    private org.springframework.test.web.servlet.ResultActions register(String email, String phone, String confirmation) throws Exception {
        return mvc.perform(post("/api/reg").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Integration User\",\"email\":\"" + email + "\",\"mobileNumber\":\"" + phone + "\",\"phoneRegion\":\"US\",\"password\":\"" + PASSWORD + "\",\"confirmPassword\":\"" + confirmation + "\"}"));
    }
    private org.springframework.test.web.servlet.ResultActions login(String identifier, String password) throws Exception {
        return mvc.perform(post("/api/login").with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}"));
    }
    private static String changeBody(String current, String next) {
        return "{\"currentPassword\":\"" + current + "\",\"newPassword\":\"" + next + "\",\"confirmPassword\":\"" + next + "\"}";
    }

    @TestConfiguration
    static class RecoveryDeliveryTestConfig {
        @Bean @org.springframework.context.annotation.Primary
        CapturingRecoveryDelivery capturingRecoveryDelivery() { return new CapturingRecoveryDelivery(); }
    }

    static class CapturingRecoveryDelivery implements RecoveryCodeDelivery {
        volatile String code;
        @Override public String channelName() { return "TEST_CAPTURE"; }
        @Override public void deliver(String value, LocalDateTime expiresAt) { code = value; }
    }
}
