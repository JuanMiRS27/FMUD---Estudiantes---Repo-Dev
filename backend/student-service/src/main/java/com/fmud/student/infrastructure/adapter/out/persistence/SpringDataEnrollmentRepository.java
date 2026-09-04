package com.fmud.student.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {
    Optional<EnrollmentEntity> findByIdAndStudentId(UUID id, UUID studentId);

    List<EnrollmentEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    boolean existsByStudentIdAndPeriodCode(UUID studentId, String periodCode);

    boolean existsByStudentIdAndPeriodCodeAndIdNot(UUID studentId, String periodCode, UUID id);
}
