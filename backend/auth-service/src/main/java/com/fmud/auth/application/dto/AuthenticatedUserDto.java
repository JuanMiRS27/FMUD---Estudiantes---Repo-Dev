package com.fmud.auth.application.dto;

import com.fmud.auth.domain.model.UserRole;

import java.util.UUID;

public record AuthenticatedUserDto(UUID id, String name, String email, UserRole role) {
}
