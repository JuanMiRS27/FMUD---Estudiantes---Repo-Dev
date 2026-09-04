package com.fmud.student.application.port.out;

import com.fmud.student.domain.history.DocumentHistoryEvent;
import com.fmud.student.domain.history.StudentHistoryEvent;

import java.util.List;
import java.util.UUID;

public interface HistoryRepositoryPort {
    StudentHistoryEvent saveStudent(StudentHistoryEvent event);

    DocumentHistoryEvent saveDocument(DocumentHistoryEvent event);

    List<StudentHistoryEvent> findStudentEventsByStudentId(UUID studentId);

    List<DocumentHistoryEvent> findDocumentEventsByStudentId(UUID studentId);
}
