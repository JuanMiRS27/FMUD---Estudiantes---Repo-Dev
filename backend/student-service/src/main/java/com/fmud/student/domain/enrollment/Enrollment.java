package com.fmud.student.domain.enrollment;

import java.time.Instant;
import java.util.UUID;

public record Enrollment(
        UUID id,
        UUID studentId,
        String periodCode,
        String program,
        EnrollmentStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
