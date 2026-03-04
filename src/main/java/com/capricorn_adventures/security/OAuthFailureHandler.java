package com.capricorn_adventures.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = exception.getMessage();
        String errorCode = "oauth2_error";

        if (errorMessage != null) {
            if (errorMessage.contains("access_denied")) {
                errorCode = "access_denied";
            } else if (errorMessage.contains("invalid_token")) {
                errorCode = "invalid_token";
            }
        }

        String redirectUrl = frontendUrl + "/signin?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
