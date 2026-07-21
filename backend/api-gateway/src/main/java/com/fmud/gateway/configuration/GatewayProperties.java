package com.fmud.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fmud.gateway")
public record GatewayProperties(String authServiceUrl, String studentServiceUrl, int timeoutSeconds) {
}
