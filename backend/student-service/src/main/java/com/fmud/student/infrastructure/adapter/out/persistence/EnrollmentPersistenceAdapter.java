package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.application.port.out.EnrollmentRepositoryPort;
import com.fmud.student.domain.enrollment.Enrollment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EnrollmentPersistenceAdapter implements EnrollmentRepositoryPort {
    private final SpringDataEnrollmentRepository repository;

    public EnrollmentPersistenceAdapter(SpringDataEnrollmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        return toDomain(repository.save(toEntity(enrollment)));
    }

    @Override
    public Optional<Enrollment> findByIdAndStudentId(UUID id, UUID studentId) {
        return repository.findByIdAndStudentId(id, studentId).map(this::toDomain);
    }

    @Override
    public List<Enrollment> findByStudentId(UUID studentId) {
        return repository.findByStudentIdOrderByCreatedAtDesc(studentId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByStudentIdAndPeriodCode(UUID studentId, String periodCode) {
        return repository.existsByStudentIdAndPeriodCode(studentId, periodCode);
    }

    @Override
    public boolean existsByStudentIdAndPeriodCodeAndIdNot(UUID studentId, String periodCode, UUID id) {
        return repository.existsByStudentIdAndPeriodCodeAndIdNot(studentId, periodCode, id);
    }

    private Enrollment toDomain(EnrollmentEntity entity) {
        return new Enrollment(entity.id, entity.studentId, entity.periodCode, entity.program, entity.status, entity.createdAt, entity.updatedAt);
    }

    private EnrollmentEntity toEntity(Enrollment enrollment) {
        EnrollmentEntity entity = new EnrollmentEntity();
        entity.id = enrollment.id();
        entity.studentId = enrollment.studentId();
        entity.periodCode = enrollment.periodCode();
        entity.program = enrollment.program();
        entity.status = enrollment.status();
        entity.createdAt = enrollment.createdAt();
        entity.updatedAt = enrollment.updatedAt();
        return entity;
    }
}
