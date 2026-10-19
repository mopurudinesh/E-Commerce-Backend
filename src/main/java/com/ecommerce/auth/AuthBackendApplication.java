package com.ecommerce.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Main entry point for the E-Commerce Authentication Backend.
 * <p>
 * This application provides enterprise-grade authentication and authorization
 * services including user registration, login with JWT tokens & HttpOnly cookies,
 * password recovery with secure OTP verification, and protected home/dashboard resources.
 */
@SpringBootApplication
public class AuthBackendApplication {

    public static void main(String[] args) {
        loadEnvFileIfExists();
        SpringApplication.run(AuthBackendApplication.class, args);
    }

    /**
     * Automatically loads key-value pairs from a local .env file (if present) into System properties.
     */
    private static void loadEnvFileIfExists() {
        File envFile = new File(".env");
        if (envFile.exists() && envFile.isFile()) {
            Properties envProps = new Properties();
            try (FileInputStream fis = new FileInputStream(envFile)) {
                envProps.load(fis);
                envProps.forEach((key, value) -> {
                    String strKey = key.toString().trim();
                    String strVal = value.toString().trim();
                    if (!strKey.isEmpty() && System.getProperty(strKey) == null) {
                        System.setProperty(strKey, strVal);
                    }
                });
            } catch (IOException e) {
                System.err.println("Could not read .env file: " + e.getMessage());
            }
        }
    }
}
