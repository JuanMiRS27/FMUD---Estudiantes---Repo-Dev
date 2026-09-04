package com.fmud.auth.application.usecase;

import com.fmud.auth.application.dto.UserDto;
import com.fmud.auth.application.port.out.PasswordHasherPort;
import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.domain.model.User;
import com.fmud.auth.domain.model.UserRole;
import com.fmud.auth.infrastructure.exception.BadRequestException;
import com.fmud.auth.infrastructure.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserManagementService {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;

    public UserManagementService(UserRepositoryPort users, PasswordHasherPort passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return users.findAll().stream().map(this::toDto).toList();
    }

    public UserDto create(String name, String email, String password, UserRole role, boolean enabled) {
        String normalizedEmail = normalizeEmail(email);
        validateName(name);
        validatePassword(password, true);
        if (users.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Ya existe un usuario registrado con este correo.");
        }
        Instant now = Instant.now();
        User user = new User(UUID.randomUUID(), name.trim(), normalizedEmail, passwordHasher.hash(password), role, enabled, now, now);
        return toDto(users.save(user));
    }

    public UserDto update(UUID id, String name, String email, String password, UserRole role, boolean enabled) {
        User current = users.findById(id).orElseThrow(() -> new NotFoundException("El usuario no existe."));
        String normalizedEmail = normalizeEmail(email);
        validateName(name);
        validatePassword(password, false);
        users.findByEmail(normalizedEmail)
                .filter(existing -> !existing.id().equals(id))
                .ifPresent(existing -> {
                    throw new BadRequestException("Ya existe un usuario registrado con este correo.");
                });
        String hash = password == null || password.isBlank() ? current.passwordHash() : passwordHasher.hash(password);
        User updated = new User(current.id(), name.trim(), normalizedEmail, hash, role, enabled, current.createdAt(), Instant.now());
        return toDto(users.save(updated));
    }

    public UserDto changeEnabled(UUID id, boolean enabled) {
        User current = users.findById(id).orElseThrow(() -> new NotFoundException("El usuario no existe."));
        User updated = new User(current.id(), current.name(), current.email(), current.passwordHash(), current.role(), enabled, current.createdAt(), Instant.now());
        return toDto(users.save(updated));
    }

    private void validateName(String name) {
        if (name == null || name.trim().length() < 2 || name.trim().length() > 120) {
            throw new BadRequestException("El nombre del usuario debe tener entre 2 y 120 caracteres.");
        }
    }

    private void validatePassword(String password, boolean required) {
        if (required && (password == null || password.length() < 8)) {
            throw new BadRequestException("La contrasena debe tener al menos 8 caracteres.");
        }
        if (!required && password != null && !password.isBlank() && password.length() < 8) {
            throw new BadRequestException("La contrasena debe tener al menos 8 caracteres.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@") || email.length() > 160) {
            throw new BadRequestException("Ingresa un correo electronico valido.");
        }
        return email.trim().toLowerCase();
    }

    private UserDto toDto(User user) {
        return new UserDto(user.id(), user.name(), user.email(), user.role(), user.enabled(), user.createdAt(), user.updatedAt());
    }
}
