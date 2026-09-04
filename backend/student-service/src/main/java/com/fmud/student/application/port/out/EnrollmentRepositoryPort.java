package com.fmud.student.application.port.out;

import com.fmud.student.domain.enrollment.Enrollment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepositoryPort {
    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findByIdAndStudentId(UUID id, UUID studentId);

    List<Enrollment> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndPeriodCode(UUID studentId, String periodCode);

    boolean existsByStudentIdAndPeriodCodeAndIdNot(UUID studentId, String periodCode, UUID id);
}
