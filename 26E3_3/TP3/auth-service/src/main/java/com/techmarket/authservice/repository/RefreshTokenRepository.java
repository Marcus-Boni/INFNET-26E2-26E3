package com.techmarket.authservice.repository;

import com.techmarket.authservice.domain.RefreshToken;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RefreshTokenRepository {

    private final Map<String, RefreshToken> tokensByTokenString = new ConcurrentHashMap<>();

    public RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken.getId() == null) {
            refreshToken.setId(UUID.randomUUID().toString());
        }
        tokensByTokenString.put(refreshToken.getToken(), refreshToken);
        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokensByTokenString.get(token));
    }

    public void deleteByToken(String token) {
        if (token != null) {
            tokensByTokenString.remove(token);
        }
    }

    public void revokeTokensForUser(String username) {
        if (username != null) {
            tokensByTokenString.values().stream()
                    .filter(t -> username.equalsIgnoreCase(t.getUsername()))
                    .forEach(t -> t.setRevoked(true));
        }
    }

    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        tokensByTokenString.entrySet().removeIf(entry -> entry.getValue().getExpiryDate().isBefore(now));
    }
}
