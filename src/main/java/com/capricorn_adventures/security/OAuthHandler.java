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

import com.capricorn_adventures.OAuth;
import com.capricorn_adventures.User;
import com.capricorn_adventures.repository.OAuthRepository;
import com.capricorn_adventures.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String providerUserId = oAuth2User.getAttribute("sub");
        String email          = oAuth2User.getAttribute("email");
        String name           = oAuth2User.getAttribute("name");
        String picture        = oAuth2User.getAttribute("picture");

        // 1. Check if OAuth account already exists
        OAuth oauth = oAuthRepository
            .findByProviderAndProviderUserId(OAuth.Provider.GOOGLE, providerUserId)
            .orElse(null);

        User user;

        if (oauth != null) {
            // Existing Google user — just log in
            user = oauth.getUser();
            log.info("Existing OAuth user logged in: {}", email);
        } else {
            // New Google user — check if email already registered
            user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Brand new user — create account
                user = User.builder()
                    .email(email)
                    .emailVerified(true)
                    .build();
                user = userRepository.save(user);
                log.info("New user created via Google OAuth: {}", email);
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
        }

        // 2. Generate tokens
        String accessToken  = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 3. Redirect to React frontend with tokens in URL
        String redirectUrl = frontendUrl + "/oauth2/redirect"
            + "?accessToken="  + URLEncoder.encode(accessToken,  StandardCharsets.UTF_8)
            + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}