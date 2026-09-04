package com.fmud.student.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Student(
        UUID id,
        String firstName,
        String lastName,
        String documentNumber,
        LocalDate birthDate,
        String birthPlace,
        String address,
        String phone,
        String email,
        StudentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
