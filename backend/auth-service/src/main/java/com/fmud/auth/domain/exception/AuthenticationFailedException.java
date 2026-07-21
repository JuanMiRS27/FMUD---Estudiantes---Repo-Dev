package com.fmud.auth.domain.exception;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException() {
        super("Credenciales incorrectas.");
    }
}
