package com.fmud.student.infrastructure.adapter.in.web;

import com.fmud.student.domain.model.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres.")
        @Pattern(regexp = "^[\\p{L} -]+$", message = "El nombre solo puede contener letras, espacios y guiones.")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(min = 2, max = 120, message = "El apellido debe tener entre 2 y 120 caracteres.")
        @Pattern(regexp = "^[\\p{L} -]+$", message = "El apellido solo puede contener letras, espacios y guiones.")
        String lastName,

        @NotBlank(message = "La cedula es obligatoria.")
        @Pattern(regexp = "\\d{6,12}", message = "La cedula debe contener unicamente numeros, entre 6 y 12 digitos.")
        String documentNumber,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        @PastOrPresent(message = "La fecha de nacimiento no puede ser futura.")
        java.time.LocalDate birthDate,

        @Size(max = 120, message = "El lugar de nacimiento no puede superar 120 caracteres.")
        String birthPlace,

        @Size(max = 180, message = "La direccion no puede superar 180 caracteres.")
        String address,

        @Pattern(regexp = "^$|\\+?[0-9](?:[0-9 ]{4,18}[0-9])?$", message = "El telefono solo puede contener digitos, espacios controlados y el simbolo + al inicio.")
        String phone,

        @Email(message = "Ingresa un correo electronico valido.")
        @Size(max = 150, message = "El correo electronico no puede superar 150 caracteres.")
        String email,

        StudentStatus status
) {
}
