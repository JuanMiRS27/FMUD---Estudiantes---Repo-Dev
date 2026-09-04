package com.fmud.auth.infrastructure.adapter.in.web;

import com.fmud.auth.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres.")
        String name,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Ingresa un correo electronico valido.")
        @Size(max = 160, message = "El correo no puede superar 160 caracteres.")
        String email,

        @Size(min = 8, max = 80, message = "La contrasena debe tener entre 8 y 80 caracteres.")
        String password,

        @NotNull(message = "El rol es obligatorio.")
        UserRole role,

        boolean enabled
) {
}
