package com.fmud.student.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fmud.security.jwt")
public record JwtProperties(String issuer, String secret) {
}
