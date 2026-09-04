package com.fmud.auth.application.usecase;

import com.fmud.auth.application.command.LoginCommand;
import com.fmud.auth.application.port.out.PasswordHasherPort;
import com.fmud.auth.application.port.out.TokenProviderPort;
import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.domain.exception.AuthenticationFailedException;
import com.fmud.auth.domain.model.User;
import com.fmud.auth.domain.model.UserRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginServiceTest {
    private final User user = new User(UUID.randomUUID(), "Usuario Secretaría", "secretario@fmud.local", "hash", UserRole.SECRETARIO, true, Instant.now(), Instant.now());

    @Test
    void authenticatesValidCredentials() {
        LoginService service = new LoginService(
                new StubUsers(Optional.of(user)),
                new StubHasher(true),
                new StubTokenProvider()
        );

        var result = service.login(new LoginCommand("secretario@fmud.local", "Cambiar123!"));

        assertThat(result.accessToken()).isEqualTo("token");
        assertThat(result.user().role()).isEqualTo(UserRole.SECRETARIO);
        assertThat(result.user().email()).isEqualTo("secretario@fmud.local");
    }

    @Test
    void rejectsWrongCredentials() {
        LoginService service = new LoginService(
                new StubUsers(Optional.of(user)),
                new StubHasher(false),
                new StubTokenProvider()
        );

        assertThatThrownBy(() -> service.login(new LoginCommand("secretario@fmud.local", "bad-password")))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    private record StubUsers(Optional<User> found) implements UserRepositoryPort {
        @Override
        public Optional<User> findByEmail(String email) {
            return found;
        }

        @Override
        public Optional<User> findById(UUID id) {
            return found;
        }

        @Override
        public List<User> findAll() {
            return found.stream().toList();
        }

        @Override
        public boolean existsByEmail(String email) {
            return found.isPresent();
        }

        @Override
        public User save(User user) {
            return user;
        }
    }

    private record StubHasher(boolean matches) implements PasswordHasherPort {
        @Override
        public boolean matches(String rawPassword, String passwordHash) {
            return matches;
        }

        @Override
        public String hash(String rawPassword) {
            return "hash";
        }
    }

    private static class StubTokenProvider implements TokenProviderPort {
        @Override
        public String createAccessToken(User user) {
            return "token";
        }

        @Override
        public long expiresInSeconds() {
            return 3600;
        }
    }
}
