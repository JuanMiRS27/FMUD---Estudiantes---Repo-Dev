package com.fmud.student.application.usecase;

import com.fmud.student.application.command.EnrollmentCommand;
import com.fmud.student.application.dto.EnrollmentDto;
import com.fmud.student.application.port.in.EnrollmentUseCase;
import com.fmud.student.application.port.out.EnrollmentRepositoryPort;
import com.fmud.student.application.port.out.StudentRepositoryPort;
import com.fmud.student.domain.enrollment.Enrollment;
import com.fmud.student.domain.enrollment.EnrollmentStatus;
import com.fmud.student.infrastructure.exception.BadRequestException;
import com.fmud.student.infrastructure.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EnrollmentService implements EnrollmentUseCase {
    private static final String PERIOD_PATTERN = "^[A-Za-z0-9._-]{1,30}$";

    private final EnrollmentRepositoryPort enrollments;
    private final StudentRepositoryPort students;

    public EnrollmentService(EnrollmentRepositoryPort enrollments, StudentRepositoryPort students) {
        this.enrollments = enrollments;
        this.students = students;
    }

    @Override
    public EnrollmentDto create(UUID studentId, EnrollmentCommand command) {
        requireStudent(studentId);
        validate(command);
        String periodCode = clean(command.periodCode());
        if (enrollments.existsByStudentIdAndPeriodCode(studentId, periodCode)) {
            throw new BadRequestException("El estudiante ya tiene una matricula registrada para ese periodo.");
        }
        Instant now = Instant.now();
        Enrollment saved = enrollments.save(new Enrollment(UUID.randomUUID(), studentId, periodCode, clean(command.program()),
                command.status() == null ? EnrollmentStatus.ENROLLED : command.status(), now, now));
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDto> list(UUID studentId) {
        requireStudent(studentId);
        return enrollments.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    @Override
    public EnrollmentDto update(UUID studentId, UUID enrollmentId, EnrollmentCommand command) {
        requireStudent(studentId);
        validate(command);
        Enrollment current = enrollments.findByIdAndStudentId(enrollmentId, studentId)
                .orElseThrow(() -> new NotFoundException("La matricula no existe."));
        String periodCode = clean(command.periodCode());
        if (enrollments.existsByStudentIdAndPeriodCodeAndIdNot(studentId, periodCode, enrollmentId)) {
            throw new BadRequestException("El estudiante ya tiene una matricula registrada para ese periodo.");
        }
        Enrollment saved = enrollments.save(new Enrollment(current.id(), studentId, periodCode, clean(command.program()),
                command.status() == null ? current.status() : command.status(), current.createdAt(), Instant.now()));
        return toDto(saved);
    }

    private void requireStudent(UUID studentId) {
        students.findById(studentId).orElseThrow(() -> new NotFoundException("El estudiante no existe."));
    }

    private void validate(EnrollmentCommand command) {
        if (command.periodCode() == null || !clean(command.periodCode()).matches(PERIOD_PATTERN)) {
            throw new BadRequestException("El periodo es obligatorio y solo puede contener letras, numeros, punto, guion y guion bajo.");
        }
        if (command.program() == null || clean(command.program()).length() < 2 || clean(command.program()).length() > 120) {
            throw new BadRequestException("El programa debe tener entre 2 y 120 caracteres.");
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private EnrollmentDto toDto(Enrollment enrollment) {
        return new EnrollmentDto(enrollment.id(), enrollment.studentId(), enrollment.periodCode(), enrollment.program(),
                enrollment.status(), enrollment.createdAt(), enrollment.updatedAt());
    }
}
