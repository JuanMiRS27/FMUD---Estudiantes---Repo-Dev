package com.fmud.auth.application.dto;

import com.fmud.auth.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        String name,
        String email,
        UserRole role,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
