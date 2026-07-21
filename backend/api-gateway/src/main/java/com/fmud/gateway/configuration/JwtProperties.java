package com.fmud.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fmud.security.jwt")
public record JwtProperties(String secret, String issuer) {
}
