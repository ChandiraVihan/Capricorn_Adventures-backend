package com.capricorn_adventures.repository;


import com.capricorn_adventures.entity.OAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface OAuthRepository extends JpaRepository<OAuth, UUID> {
    @Query("SELECT o FROM OAuth o JOIN FETCH o.user WHERE o.provider = :provider AND o.providerUserId = :providerUserId")
    Optional<OAuth> findWithUserByProviderAndProviderUserId(
        @Param("provider") OAuth.Provider provider, 
        @Param("providerUserId") String providerUserId);

    Optional<OAuth> findByProviderAndProviderUserId(
        OAuth.Provider provider, String providerUserId);
    Optional<OAuth> findByProviderEmailAndProvider(
        String email, OAuth.Provider provider);
}
