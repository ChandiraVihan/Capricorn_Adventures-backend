package com.capricorn_adventures.config;


import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.capricorn_adventures.security.JwtFilter;
import com.capricorn_adventures.security.OAuthHandler;
import com.capricorn_adventures.security.OAuthFailureHandler;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final OAuthHandler oAuthHandler;
    private final OAuthFailureHandler oAuthFailureHandler;

    public SecurityConfig(JwtFilter jwtFilter, OAuthHandler oAuthHandler, OAuthFailureHandler oAuthFailureHandler) {
        this.jwtFilter = jwtFilter;
        this.oAuthHandler = oAuthHandler;
        this.oAuthFailureHandler = oAuthFailureHandler;
    }

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

  
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Exception handling for unauthorized access
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(401, "Unauthorized");
                })
            )
    
            .authorizeHttpRequests(auth -> auth
                // 1. Explicitly public routes
                .requestMatchers(
                   "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/v1/rooms/search",
                    "/api/v1/rooms/**",
                    "/api/finance/**",
                    "/api/manager/operations/**",
                    "/api/room-service/orders/dashboard",
                    "/api/room-service/orders/dashboard/**",
                    "/api/room-service/orders/daily-summary",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/api/webhooks/**"
                ).permitAll()
    
                // 2. Fallback: Authenticated for any other request
                .anyRequest().authenticated()
            )
    
            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(a -> a.baseUri("/oauth2/authorize"))
                .redirectionEndpoint(r -> r.baseUri("/oauth2/callback/*"))
                .successHandler(oAuthHandler)
                .failureHandler(oAuthFailureHandler)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Cost factor 12 as per DB design
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
