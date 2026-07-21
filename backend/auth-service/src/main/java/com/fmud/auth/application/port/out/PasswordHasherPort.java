package com.fmud.auth.application.port.out;

public interface PasswordHasherPort {
    boolean matches(String rawPassword, String passwordHash);

    String hash(String rawPassword);
}
