package com.fmud.student.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataHistoryRepository extends JpaRepository<StudentHistoryEventEntity, UUID> {
    List<StudentHistoryEventEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
