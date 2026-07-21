package com.fmud.auth.infrastructure.security;

import com.fmud.auth.domain.model.User;
import com.fmud.auth.domain.model.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {
    @Test
    void createsTokenWithRequiredClaims() {
        JwtTokenProvider provider = new JwtTokenProvider(new JwtProperties("test-secret-with-at-least-32-characters", "fmud-auth-service", 3600));
        User user = new User(UUID.randomUUID(), "Usuario Junta", "admin@fmud.local", "hash", UserRole.ADMIN, true, Instant.now(), Instant.now());

        JwtPrincipal principal = provider.parse(provider.createAccessToken(user));

        assertThat(principal.id()).isEqualTo(user.id());
        assertThat(principal.email()).isEqualTo("admin@fmud.local");
        assertThat(principal.role()).isEqualTo("ADMIN");
    }
}
