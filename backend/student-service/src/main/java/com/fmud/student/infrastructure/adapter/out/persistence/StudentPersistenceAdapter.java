package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.application.port.out.DocumentRepositoryPort;
import com.fmud.student.application.port.out.HistoryRepositoryPort;
import com.fmud.student.application.port.out.StudentRepositoryPort;
import com.fmud.student.domain.document.DocumentStatus;
import com.fmud.student.domain.document.StudentDocument;
import com.fmud.student.domain.history.DocumentHistoryEvent;
import com.fmud.student.domain.history.StudentHistoryEvent;
import com.fmud.student.domain.model.Student;
import com.fmud.student.domain.model.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StudentPersistenceAdapter implements StudentRepositoryPort, DocumentRepositoryPort, HistoryRepositoryPort {
    private final SpringDataStudentRepository students;
    private final SpringDataDocumentRepository documents;
    private final SpringDataHistoryRepository history;
    private final SpringDataDocumentHistoryRepository documentHistory;

    public StudentPersistenceAdapter(SpringDataStudentRepository students, SpringDataDocumentRepository documents, SpringDataHistoryRepository history,
                                     SpringDataDocumentHistoryRepository documentHistory) {
        this.students = students;
        this.documents = documents;
        this.history = history;
        this.documentHistory = documentHistory;
    }

    @Override
    public Student save(Student student) {
        return toDomain(students.save(toEntity(student)));
    }

    @Override
    public Optional<Student> findById(UUID id) {
        return students.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return students.existsByDocumentNumber(documentNumber);
    }

    @Override
    public boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id) {
        return students.existsByDocumentNumberAndIdNot(documentNumber, id);
    }

    @Override
    public Page<Student> search(String search, StudentStatus status, Pageable pageable) {
        return students.search(search, status, pageable).map(this::toDomain);
    }

    @Override
    public StudentDocument save(StudentDocument document) {
        return toDomain(documents.save(toEntity(document)));
    }

    @Override
    public Optional<StudentDocument> findByIdAndStudentId(UUID id, UUID studentId) {
        return documents.findByIdAndStudentId(id, studentId).map(this::toDomain);
    }

    @Override
    public Optional<StudentDocument> findActiveByStudentIdAndType(UUID studentId, String type) {
        return documents.findFirstByStudentIdAndStatusAndDocumentTypeOrderByCreatedAtDesc(studentId, DocumentStatus.ACTIVE, type).map(this::toDomain);
    }

    @Override
    public List<StudentDocument> findActiveByStudentId(UUID studentId, String type) {
        List<StudentDocumentEntity> rows = (type == null || type.isBlank())
                ? documents.findByStudentIdAndStatusOrderByCreatedAtDesc(studentId, DocumentStatus.ACTIVE)
                : documents.findByStudentIdAndStatusAndDocumentTypeOrderByCreatedAtDesc(studentId, DocumentStatus.ACTIVE, type);
        return rows.stream().map(this::toDomain).toList();
    }

    @Override
    public StudentHistoryEvent saveStudent(StudentHistoryEvent event) {
        return toDomain(history.save(toEntity(event)));
    }

    @Override
    public DocumentHistoryEvent saveDocument(DocumentHistoryEvent event) {
        return toDomain(documentHistory.save(toEntity(event)));
    }

    @Override
    public List<StudentHistoryEvent> findStudentEventsByStudentId(UUID studentId) {
        return history.findByStudentIdOrderByCreatedAtDesc(studentId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<DocumentHistoryEvent> findDocumentEventsByStudentId(UUID studentId) {
        return documentHistory.findByStudentIdOrderByCreatedAtDesc(studentId).stream().map(this::toDomain).toList();
    }

    private Student toDomain(StudentEntity entity) {
        return new Student(entity.id, entity.firstName, entity.lastName, entity.documentNumber, entity.birthDate, entity.birthPlace,
                entity.address, entity.phone, entity.email, entity.status, entity.createdAt, entity.updatedAt);
    }

    private StudentEntity toEntity(Student student) {
        StudentEntity entity = new StudentEntity();
        entity.id = student.id();
        entity.firstName = student.firstName();
        entity.lastName = student.lastName();
        entity.documentNumber = student.documentNumber();
        entity.birthDate = student.birthDate();
        entity.birthPlace = student.birthPlace();
        entity.address = student.address();
        entity.phone = student.phone();
        entity.email = student.email();
        entity.status = student.status();
        entity.createdAt = student.createdAt();
        entity.updatedAt = student.updatedAt();
        return entity;
    }

    private StudentDocument toDomain(StudentDocumentEntity entity) {
        return new StudentDocument(entity.id, entity.studentId, entity.documentType, entity.displayName, entity.originalName, entity.storageKey,
                entity.contentType, entity.sizeBytes, entity.description, entity.status, entity.uploadedByUserId, entity.createdAt, entity.updatedAt);
    }

    private StudentDocumentEntity toEntity(StudentDocument document) {
        StudentDocumentEntity entity = new StudentDocumentEntity();
        entity.id = document.id();
        entity.studentId = document.studentId();
        entity.documentType = document.documentType();
        entity.displayName = document.displayName();
        entity.originalName = document.originalName();
        entity.storageKey = document.storageKey();
        entity.contentType = document.contentType();
        entity.sizeBytes = document.size();
        entity.description = document.description();
        entity.status = document.status();
        entity.uploadedByUserId = document.uploadedByUserId();
        entity.createdAt = document.createdAt();
        entity.updatedAt = document.updatedAt();
        return entity;
    }

    private StudentHistoryEvent toDomain(StudentHistoryEventEntity entity) {
        return new StudentHistoryEvent(entity.id, entity.studentId, entity.actorUserId, entity.action, entity.summary, entity.createdAt);
    }

    private StudentHistoryEventEntity toEntity(StudentHistoryEvent event) {
        StudentHistoryEventEntity entity = new StudentHistoryEventEntity();
        entity.id = event.id();
        entity.studentId = event.studentId();
        entity.actorUserId = event.actorUserId();
        entity.action = event.action();
        entity.summary = event.summary();
        entity.createdAt = event.createdAt();
        return entity;
    }

    private DocumentHistoryEvent toDomain(DocumentHistoryEventEntity entity) {
        return new DocumentHistoryEvent(entity.id, entity.documentId, entity.actorUserId, entity.action, entity.summary, entity.createdAt);
    }

    private DocumentHistoryEventEntity toEntity(DocumentHistoryEvent event) {
        DocumentHistoryEventEntity entity = new DocumentHistoryEventEntity();
        entity.id = event.id();
        entity.documentId = event.documentId();
        entity.actorUserId = event.actorUserId();
        entity.action = event.action();
        entity.summary = event.summary();
        entity.createdAt = event.createdAt();
        return entity;
    }
}
