package com.fmud.student.infrastructure.adapter.in.web;

import com.fmud.student.domain.enrollment.EnrollmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnrollmentRequest(
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "^[A-Za-z0-9._-]{1,30}$")
        String periodCode,
        @NotBlank
        @Size(max = 120)
        String program,
        EnrollmentStatus status
) {
}
