package com.fmud.student.application.port.in;

import com.fmud.student.application.command.EnrollmentCommand;
import com.fmud.student.application.dto.EnrollmentDto;

import java.util.List;
import java.util.UUID;

public interface EnrollmentUseCase {
    EnrollmentDto create(UUID studentId, EnrollmentCommand command);

    List<EnrollmentDto> list(UUID studentId);

    EnrollmentDto update(UUID studentId, UUID enrollmentId, EnrollmentCommand command);
}
