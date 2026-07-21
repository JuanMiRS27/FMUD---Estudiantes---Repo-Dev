package com.fmud.auth.application.dto;

public record LoginResultDto(
        String accessToken,
        String tokenType,
        long expiresIn,
        AuthenticatedUserDto user
) {
}
