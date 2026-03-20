package com.capricorn_adventures.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.capricorn_adventures.entity.OAuth;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.repository.OAuthRepository;
import com.capricorn_adventures.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Transactional;

@Component
public class OAuthHandler extends SimpleUrlAuthenticationSuccessHandler {

    // ── Logger (replaces Lombok @Slf4j) ──────────────────────────
    private static final Logger log = LoggerFactory.getLogger(OAuthHandler.class);

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final JwtUtil jwtUtil;

    // ── Constructor injection (replaces Lombok @RequiredArgsConstructor) ──
    public OAuthHandler(UserRepository userRepository,
                        OAuthRepository oAuthRepository,
                        JwtUtil jwtUtil) {
        this.userRepository  = userRepository;
        this.oAuthRepository = oAuthRepository;
        this.jwtUtil         = jwtUtil;
    }

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String providerUserId = oAuth2User.getAttribute("sub");
        String email          = oAuth2User.getAttribute("email");
        String name           = oAuth2User.getAttribute("name");
        String picture        = oAuth2User.getAttribute("picture");

        if (providerUserId == null || email == null) {
            log.error("OAuth2 attributes missing. sub: {}, email: {}", providerUserId, email);
            throw new RuntimeException("Required OAuth2 attributes (sub/email) missing from Google.");
        }

        // 1. Check if OAuth account already exists
        // Use JOIN FETCH to avoid lazy loading issues with the linked User
        OAuth oauth = oAuthRepository
            .findWithUserByProviderAndProviderUserId(OAuth.Provider.GOOGLE, providerUserId)
            .orElse(null);

        User user;

        if (oauth != null) {
            // Existing Google user. The user object is already joined and loaded.
            user = oauth.getUser();
            log.info("Existing OAuth user logged in: {} (ID: {})", email, user.getId());
            
            // Sync email if changed on Google and not already taken
            if (!user.getEmail().equalsIgnoreCase(email)) {
                log.info("Updating user {} email from {} to {}", user.getId(), user.getEmail(), email);
                if (!userRepository.existsByEmail(email)) {
                    user.setEmail(email);
                    user = userRepository.save(user);
                } else {
                    log.warn("Cannot update email to {} as it's already taken by another user", email);
                }
            }
        } else {
            // New Google connection — check if email already registered
            user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Brand new user — create account
                user = User.builder()
                    .email(email)
                    .emailVerified(true)
                    .role(User.UserRole.CUSTOMER)
                    .build();
                user = userRepository.save(user);
                log.info("New user created via Google OAuth: {}", email);
            } else {
                log.info("Found existing user {} by email. Linking Google account.", email);
            }

            // Link the Google account
            OAuth newOauth = OAuth.builder()
                .user(user)
                .provider(OAuth.Provider.GOOGLE)
                .providerUserId(providerUserId)
                .providerEmail(email)
                .providerName(name)
                .avatarUrl(picture)
                .build();
            oAuthRepository.save(newOauth);
            log.info("Linked Google account sub:{} to user:{}", providerUserId, user.getId());
        }

        // 2. Ensure user has a role and is active before token generation
        if (user.getRole() == null) {
            log.warn("User {} has no role, defaulting to CUSTOMER", user.getEmail());
            user.setRole(User.UserRole.CUSTOMER);
            user = userRepository.save(user);
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.error("Login attempt for non-active user: {}", user.getEmail());
            throw new RuntimeException("Account is not active (" + user.getStatus() + ")");
        }

        // 3. Update last login time
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        // 4. Generate tokens
        String accessToken  = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 5. Redirect to React frontend with tokens in URL
        String redirectUrl = frontendUrl + "/oauth2/redirect"
            + "?accessToken="  + URLEncoder.encode(accessToken,  StandardCharsets.UTF_8)
            + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        log.info("Redirecting user {} to frontend with JWT", user.getEmail());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}