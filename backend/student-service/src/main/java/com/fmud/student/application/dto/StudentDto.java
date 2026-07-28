package com.fmud.student.application.dto;

import com.fmud.student.domain.model.StudentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StudentDto(
        UUID id,
        String firstName,
        String lastName,
        String documentNumber,
        LocalDate birthDate,
        String birthPlace,
        String address,
        String phone,
        String email,
        String photoUrl,
        StudentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
