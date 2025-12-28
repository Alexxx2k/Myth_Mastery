package com.alexxx2k.springproject.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {

    public static void main(String[] args) {
        String password = "12345";

        System.out.println("=== BCrypt Password Hash Generator ===");
        System.out.println("Password: " + password);
        System.out.println("=====================================\n");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(password);
            boolean matches = encoder.matches(password, hash);

            System.out.println("--- Hash " + i + " ---");
            System.out.println("Hash: " + hash);
            System.out.println("Length: " + hash.length() + " chars");
            System.out.println("Verification: " + (matches ? "✓ SUCCESS" : "✗ FAILED"));

            if (matches) {
                System.out.println("\n=== COPY THIS TO CustomUserDetailsService.java ===");
                System.out.println("private static final String ADMIN_PASSWORD_HASH = \"" + hash + "\";");
                System.out.println("===================================================\n");
            }
        }

        System.out.println("\n=== EXISTING HASH VERIFICATION ===");
        String existingHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing hash: " + existingHash);
        System.out.println("Password matches existing hash: " + existingMatches);
    }

    public static String generateHash(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(password);
    }

    public static boolean verifyHash(String password, String hash) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(password, hash);
    }
}
