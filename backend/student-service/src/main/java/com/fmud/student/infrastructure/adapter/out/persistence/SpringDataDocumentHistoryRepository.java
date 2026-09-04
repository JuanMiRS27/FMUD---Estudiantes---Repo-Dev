package com.fmud.student.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

interface SpringDataDocumentHistoryRepository extends JpaRepository<DocumentHistoryEventEntity, UUID> {
    @Query("""
            select h
            from DocumentHistoryEventEntity h
            join StudentDocumentEntity d on d.id = h.documentId
            where d.studentId = :studentId
            order by h.createdAt desc
            """)
    List<DocumentHistoryEventEntity> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
