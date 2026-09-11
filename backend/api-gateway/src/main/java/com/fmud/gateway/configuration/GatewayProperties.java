package com.fmud.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "fmud.gateway")
public record GatewayProperties(String authServiceUrl, String studentServiceUrl, int timeoutSeconds, List<String> allowedOrigins) {
}
