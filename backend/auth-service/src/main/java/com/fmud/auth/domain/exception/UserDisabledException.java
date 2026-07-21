package com.fmud.auth.domain.exception;

public class UserDisabledException extends RuntimeException {
    public UserDisabledException() {
        super("El usuario se encuentra deshabilitado.");
    }
}
