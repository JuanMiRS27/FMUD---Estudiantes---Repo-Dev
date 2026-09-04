package com.fmud.student.application.port.out;

import com.fmud.student.application.dto.ResumeDetailsDto;

import java.util.UUID;

public interface ResumeDetailsRepositoryPort {
    ResumeDetailsDto findByStudentId(UUID studentId);

    void save(UUID studentId, ResumeDetailsDto details);
}
