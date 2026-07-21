package com.fmud.auth.application.port.in;

import com.fmud.auth.application.dto.AuthenticatedUserDto;

import java.util.UUID;

public interface GetCurrentUserUseCase {
    AuthenticatedUserDto getById(UUID userId);
}
