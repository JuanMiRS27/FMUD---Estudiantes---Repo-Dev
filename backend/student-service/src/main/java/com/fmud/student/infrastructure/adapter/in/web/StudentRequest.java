package com.fmud.student.infrastructure.adapter.in.web;

import com.fmud.student.domain.model.StudentStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record StudentRequest(
        @NotBlank @Size(min = 2, max = 80) @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ -]+$") String firstName,
        @NotBlank @Size(min = 2, max = 120) @Pattern(regexp = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ -]+$") String lastName,
        @NotBlank @Pattern(regexp = "\\d{6,12}") String documentNumber,
        @NotNull @PastOrPresent LocalDate birthDate,
        @Size(max = 120) String birthPlace,
        @Size(max = 180) String address,
        @Pattern(regexp = "^[0-9+]{0,20}$") String phone,
        @Email @Size(max = 150) String email,
        StudentStatus status
) {
}
