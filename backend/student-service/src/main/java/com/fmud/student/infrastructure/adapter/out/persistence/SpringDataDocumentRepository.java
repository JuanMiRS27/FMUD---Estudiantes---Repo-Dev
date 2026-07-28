package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.domain.document.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface SpringDataDocumentRepository extends JpaRepository<StudentDocumentEntity, UUID> {
    Optional<StudentDocumentEntity> findByIdAndStudentId(UUID id, UUID studentId);

    List<StudentDocumentEntity> findByStudentIdAndStatusOrderByCreatedAtDesc(UUID studentId, DocumentStatus status);

    List<StudentDocumentEntity> findByStudentIdAndStatusAndDocumentTypeOrderByCreatedAtDesc(UUID studentId, DocumentStatus status, String documentType);
}
