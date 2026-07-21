package com.fmud.auth.application.port.out;

import com.fmud.auth.domain.model.User;

public interface TokenProviderPort {
    String createAccessToken(User user);

    long expiresInSeconds();
}
