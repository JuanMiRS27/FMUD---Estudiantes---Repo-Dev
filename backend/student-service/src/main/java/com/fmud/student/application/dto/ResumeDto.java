package com.fmud.student.application.dto;

import java.util.List;

public record ResumeDto(
        StudentDto student,
        List<DocumentDto> documents,
        List<HistoryEventDto> history
) {
}
