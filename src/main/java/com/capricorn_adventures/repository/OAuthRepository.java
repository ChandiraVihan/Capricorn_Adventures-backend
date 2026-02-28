package com.capricorn_adventures.repository;


import com.capricorn_adventures.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OAuthRepository extends JpaRepository<OAuth, UUID> {
    Optional<OAuth> findByProviderAndProviderUserId(
        OAuth.Provider provider, String providerUserId);
    Optional<OAuth> findByProviderEmailAndProvider(
        String email, OAuth.Provider provider);
}
