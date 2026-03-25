package com.capricorn_adventures.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mail.MailException;
import com.capricorn_adventures.dto.*;
import com.capricorn_adventures.entity.PasswordResetToken;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.repository.PasswordResetTokenRepository;
import com.capricorn_adventures.repository.UserRepository;
import com.capricorn_adventures.security.JwtUtil;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository resetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       JavaMailSender mailSender) {
        this.userRepository       = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder      = passwordEncoder;
        this.jwtUtil              = jwtUtil;
        this.mailSender           = mailSender;
    }

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ── REGISTER ──────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> register(String email, String password,
                                        String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
            .email(email.toLowerCase().trim())
            .firstName(firstName)
            .lastName(lastName)
            .passwordHash(passwordEncoder.encode(password))
            .build();
        user = userRepository.save(user);
        
        sendWelcomeEmail(user.getEmail(), firstName);
        
        // TODO: send verification email (email must be verified)
        log.info("New user registered: {}", email);

        return buildAuthResponse(user);
    }

    // ── LOGIN ─────────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is suspended or deleted");
        }

        if (user.isLocked()) {
            throw new RuntimeException("Account locked. Try again after 15 minutes.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.incrementFailedLogin();
            userRepository.save(user);

            int remaining = Math.max(0, 5 - user.getFailedLoginCount());
            throw new RuntimeException(remaining > 0
                ? "Invalid email or password. " + remaining + " attempts remaining."
                : "Account locked due to too many failed attempts. Try again in 15 minutes.");
        }

        user.resetFailedLogin();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {}", email);
        return buildAuthResponse(user);
    }

    // ── REFRESH TOKEN ─────────────────────────────────────────────
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // ✅ Fixed: explicit UUID type instead of var — removes unchecked warning
        UUID userId = jwtUtil.extractUserId(refreshToken);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is not active");
        }

        String newAccessToken = jwtUtil.generateAccessToken(
            user.getId(), user.getEmail(), user.getRole().name()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        return response;
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
            .orElse(null);

        // Always return silently — don't reveal whether email exists
        if (user == null) return;

        resetTokenRepository.invalidateAllForUser(user.getId());

        String rawToken  = generateSecureToken();
        String tokenHash = sha256(rawToken);

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .user(user)
            .tokenHash(tokenHash)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();
        resetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
        sendPasswordResetEmail(user.getEmail(), resetLink);

        log.info("Password reset requested for: {}", email);
    }

    // ── RESET PASSWORD ────────────────────────────────────────────
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = sha256(rawToken);

        PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (!resetToken.isValid()) {
            throw new RuntimeException("Reset link has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.resetFailedLogin();
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        resetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getId());
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────

    private Map<String, Object> buildAuthResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(
            user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id",            user.getId().toString());
        userInfo.put("email",         user.getEmail());
        userInfo.put("firstName",     user.getFirstName());
        userInfo.put("lastName",      user.getLastName());
        userInfo.put("role",          user.getRole().name());
        userInfo.put("emailVerified", user.isEmailVerified());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken",  accessToken);
        response.put("refreshToken", refreshToken);
        response.put("user",         userInfo);
        return response;
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) hexString.append(String.format("%02x", b));
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    // TODO: replace with HTML email template later
    private void sendPasswordResetEmail(String to, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Reset your Capricorn Adventures password");
            message.setText(
                "Hi,\n\n" +
                "We received a request to reset your password.\n\n" +
                "Click the link below to set a new password (valid for 1 hour):\n\n" +
                resetLink + "\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "— Capricorn Adventures Team"
            );
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Unable to send password reset email to {}: {}", to, ex.getMessage());
        }
    }

    private void sendWelcomeEmail(String to, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Welcome to Capricorn Adventures!");
            message.setText(
                "Hi " + firstName + ",\n\n" +
                "Welcome to Capricorn Adventures! We're thrilled to have you join our community.\n\n" +
                "You can now sign in to explore our curated hotel collections and plan your next adventure.\n\n" +
                "If you have any questions, just reply to this email.\n\n" +
                "Happy exploring!\n\n" +
                "— Capricorn Adventures Team"
            );
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Unable to send welcome email to {}: {}", to, ex.getMessage());
        }
    }
}