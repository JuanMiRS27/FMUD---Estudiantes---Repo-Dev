package com.fmud.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String name,
        String email,
        String passwordHash,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
