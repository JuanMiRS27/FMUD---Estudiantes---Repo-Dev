package com.fmud.auth.application.usecase;

import com.fmud.auth.application.command.LoginCommand;
import com.fmud.auth.application.dto.AuthenticatedUserDto;
import com.fmud.auth.application.dto.LoginResultDto;
import com.fmud.auth.application.port.in.LoginUseCase;
import com.fmud.auth.application.port.out.PasswordHasherPort;
import com.fmud.auth.application.port.out.TokenProviderPort;
import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.domain.exception.AuthenticationFailedException;
import com.fmud.auth.domain.exception.UserDisabledException;
import com.fmud.auth.domain.model.User;

public class LoginService implements LoginUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public LoginService(UserRepositoryPort users, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        this.users = users;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public LoginResultDto login(LoginCommand command) {
        User user = users.findByEmail(command.email().trim().toLowerCase())
                .orElseThrow(AuthenticationFailedException::new);

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new AuthenticationFailedException();
        }
        if (!user.enabled()) {
            throw new UserDisabledException();
        }

        return new LoginResultDto(
                tokenProvider.createAccessToken(user),
                "Bearer",
                tokenProvider.expiresInSeconds(),
                new AuthenticatedUserDto(user.id(), user.name(), user.email(), user.role())
        );
    }
}
