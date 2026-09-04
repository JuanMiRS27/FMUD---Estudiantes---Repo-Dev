package com.fmud.student.application.command;

import com.fmud.student.domain.enrollment.EnrollmentStatus;

public record EnrollmentCommand(
        String periodCode,
        String program,
        EnrollmentStatus status
) {
}
