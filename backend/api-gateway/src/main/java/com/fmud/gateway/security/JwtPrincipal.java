package com.fmud.gateway.security;

import java.util.UUID;

public record JwtPrincipal(UUID id, String email, String name, String role) {
}
