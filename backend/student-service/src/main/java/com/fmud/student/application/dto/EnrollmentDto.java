package com.fmud.student.application.dto;

import com.fmud.student.domain.enrollment.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentDto(
        UUID id,
        UUID studentId,
        String periodCode,
        String program,
        EnrollmentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
