package com.fmud.student.application.port.out;

import com.fmud.student.domain.document.StudentDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepositoryPort {
    StudentDocument save(StudentDocument document);

    Optional<StudentDocument> findByIdAndStudentId(UUID id, UUID studentId);

    List<StudentDocument> findActiveByStudentId(UUID studentId, String type);
}
