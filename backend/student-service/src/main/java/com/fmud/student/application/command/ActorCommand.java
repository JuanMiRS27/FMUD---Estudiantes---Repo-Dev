package com.fmud.student.application.command;

import java.util.UUID;

public record ActorCommand(UUID userId, String name, String role) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
