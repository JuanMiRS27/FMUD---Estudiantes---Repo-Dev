package com.fmud.auth.domain.model;

public enum UserRole {
    SECRETARIO("Secretaría"),
    ADMIN("Junta Administrativa");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
