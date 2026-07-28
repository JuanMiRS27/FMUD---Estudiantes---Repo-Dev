package com.fmud.auth.infrastructure.configuration;

import com.fmud.auth.application.port.out.PasswordHasherPort;
import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.domain.model.User;
import com.fmud.auth.domain.model.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Profile({"dev", "docker"})
@EnableConfigurationProperties(DevUsersSeeder.DevUsersProperties.class)
public class DevUsersSeeder implements ApplicationRunner {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final DevUsersProperties properties;

    public DevUsersSeeder(UserRepositoryPort users, PasswordHasherPort passwordHasher, DevUsersProperties properties) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfMissing("Usuario Secretaria", "secretario@fmud.local", properties.secretarioPassword(), UserRole.SECRETARIO);
        createIfMissing("Usuario Junta Administrativa", "admin@fmud.local", properties.adminPassword(), UserRole.ADMIN);
    }

    private void createIfMissing(String name, String email, String password, UserRole role) {
        if (users.existsByEmail(email)) {
            return;
        }
        Instant now = Instant.now();
        users.save(new User(UUID.randomUUID(), name, email, passwordHasher.hash(password), role, true, now, now));
    }

    @ConfigurationProperties(prefix = "fmud.dev-users")
    public record DevUsersProperties(String secretarioPassword, String adminPassword) {
    }
}
