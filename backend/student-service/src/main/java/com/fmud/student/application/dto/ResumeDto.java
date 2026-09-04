package com.fmud.student.application.dto;

import java.util.List;

public record ResumeDto(
        StudentDto student,
        ResumeDetailsDto details,
        List<EnrollmentDto> enrollments,
        List<DocumentDto> documents,
        List<HistoryEventDto> history
) {
}
