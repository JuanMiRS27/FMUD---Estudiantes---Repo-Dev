package com.fmud.auth.application.usecase;

import com.fmud.auth.application.dto.AuthenticatedUserDto;
import com.fmud.auth.application.port.in.GetCurrentUserUseCase;
import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.domain.exception.AuthenticationFailedException;

import java.util.UUID;

public class GetCurrentUserService implements GetCurrentUserUseCase {
    private final UserRepositoryPort users;

    public GetCurrentUserService(UserRepositoryPort users) {
        this.users = users;
    }

    @Override
    public AuthenticatedUserDto getById(UUID userId) {
        return users.findById(userId)
                .filter(user -> user.enabled())
                .map(user -> new AuthenticatedUserDto(user.id(), user.name(), user.email(), user.role()))
                .orElseThrow(AuthenticationFailedException::new);
    }
}
