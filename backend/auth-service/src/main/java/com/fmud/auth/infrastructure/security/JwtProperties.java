package com.fmud.auth.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fmud.security.jwt")
public record JwtProperties(String secret, String issuer, long expirationSeconds) {
}
