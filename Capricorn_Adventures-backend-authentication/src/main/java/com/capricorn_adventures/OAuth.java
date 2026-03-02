package com.capricorn_adventures;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "oauth",
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
public class OAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    @Column(name = "provider_name", length = 255)
    private String providerName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OAuth() {}

    public OAuth(UUID id, User user, Provider provider, String providerUserId, String providerEmail, String providerName, String avatarUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.providerEmail = providerEmail;
        this.providerName = providerName;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Provider getProvider() { return provider; }
    public void setProvider(Provider provider) { this.provider = provider; }

    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    public String getProviderEmail() { return providerEmail; }
    public void setProviderEmail(String providerEmail) { this.providerEmail = providerEmail; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum Provider { GOOGLE, APPLE, FACEBOOK }

    public static OAuthBuilder builder() {
        return new OAuthBuilder();
    }

    public static class OAuthBuilder {
        private UUID id;
        private User user;
        private Provider provider;
        private String providerUserId;
        private String providerEmail;
        private String providerName;
        private String avatarUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        OAuthBuilder() {}

        public OAuthBuilder id(UUID id) { this.id = id; return this; }
        public OAuthBuilder user(User user) { this.user = user; return this; }
        public OAuthBuilder provider(Provider provider) { this.provider = provider; return this; }
        public OAuthBuilder providerUserId(String providerUserId) { this.providerUserId = providerUserId; return this; }
        public OAuthBuilder providerEmail(String providerEmail) { this.providerEmail = providerEmail; return this; }
        public OAuthBuilder providerName(String providerName) { this.providerName = providerName; return this; }
        public OAuthBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public OAuthBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OAuthBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OAuth build() {
            return new OAuth(id, user, provider, providerUserId, providerEmail, providerName, avatarUrl, createdAt, updatedAt);
        }
    }
}
