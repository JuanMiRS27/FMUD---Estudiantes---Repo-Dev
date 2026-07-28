package com.fmud.student.application.usecase;

import com.fmud.student.application.command.ActorCommand;
import com.fmud.student.application.command.DocumentCommand;
import com.fmud.student.application.command.StudentCommand;
import com.fmud.student.application.dto.*;
import com.fmud.student.application.port.in.StudentResumeUseCase;
import com.fmud.student.application.port.out.DocumentRepositoryPort;
import com.fmud.student.application.port.out.FileStoragePort;
import com.fmud.student.application.port.out.HistoryRepositoryPort;
import com.fmud.student.application.port.out.StudentRepositoryPort;
import com.fmud.student.domain.document.DocumentStatus;
import com.fmud.student.domain.document.StudentDocument;
import com.fmud.student.domain.history.HistoryAction;
import com.fmud.student.domain.history.HistoryEvent;
import com.fmud.student.domain.model.Student;
import com.fmud.student.domain.model.StudentStatus;
import com.fmud.student.infrastructure.exception.BadRequestException;
import com.fmud.student.infrastructure.exception.ForbiddenException;
import com.fmud.student.infrastructure.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StudentResumeService implements StudentResumeUseCase {
    private static final String NAME_PATTERN = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ -]+$";
    private final StudentRepositoryPort students;
    private final DocumentRepositoryPort documents;
    private final HistoryRepositoryPort history;
    private final FileStoragePort storage;

    public StudentResumeService(StudentRepositoryPort students, DocumentRepositoryPort documents, HistoryRepositoryPort history, FileStoragePort storage) {
        this.students = students;
        this.documents = documents;
        this.history = history;
        this.storage = storage;
    }

    @Override
    public StudentDto create(StudentCommand command, ActorCommand actor) {
        validate(command);
        if (students.existsByDocumentNumber(command.documentNumber())) {
            throw new BadRequestException("La cedula ya se encuentra registrada.");
        }
        Instant now = Instant.now();
        FileStoragePort.StoredFile photo = command.photo() == null || command.photo().isEmpty() ? null : storage.storePhoto(command.photo());
        Student student = new Student(UUID.randomUUID(), clean(command.firstName()), clean(command.lastName()), command.documentNumber(),
                command.birthDate(), clean(command.birthPlace()), clean(command.address()), clean(command.phone()), clean(command.email()),
                photo == null ? null : photo.storageKey(), photo == null ? null : photo.contentType(),
                command.status() == null ? StudentStatus.ACTIVE : command.status(), now, now);
        Student saved = students.save(student);
        audit(saved.id(), actor, HistoryAction.STUDENT_CREATED, "STUDENT", saved.id(), "Estudiante creado.");
        return toDto(saved);
    }

    @Override
    public StudentDto update(UUID id, StudentCommand command, ActorCommand actor) {
        Student current = getDomain(id);
        validate(command);
        if (students.existsByDocumentNumberAndIdNot(command.documentNumber(), id)) {
            throw new BadRequestException("La cedula ya se encuentra registrada.");
        }
        FileStoragePort.StoredFile photo = command.photo() == null || command.photo().isEmpty() ? null : storage.storePhoto(command.photo());
        Student updated = new Student(current.id(), clean(command.firstName()), clean(command.lastName()), command.documentNumber(), command.birthDate(),
                clean(command.birthPlace()), clean(command.address()), clean(command.phone()), clean(command.email()),
                photo == null ? current.photoStorageKey() : photo.storageKey(), photo == null ? current.photoContentType() : photo.contentType(),
                command.status() == null ? current.status() : command.status(), current.createdAt(), Instant.now());
        Student saved = students.save(updated);
        audit(id, actor, HistoryAction.STUDENT_UPDATED, "STUDENT", id, "Informacion del estudiante actualizada.");
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentDto> list(String search, StudentStatus status, Pageable pageable) {
        var page = students.search(search, status, pageable).map(this::toDto);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto get(UUID id) {
        return toDto(getDomain(id));
    }

    @Override
    public StudentDto changeStatus(UUID id, StudentStatus status, ActorCommand actor) {
        if (!actor.isAdmin()) {
            throw new ForbiddenException("Solo Junta Administrativa puede desactivar estudiantes.");
        }
        Student current = getDomain(id);
        Student updated = new Student(current.id(), current.firstName(), current.lastName(), current.documentNumber(), current.birthDate(), current.birthPlace(),
                current.address(), current.phone(), current.email(), current.photoStorageKey(), current.photoContentType(), status, current.createdAt(), Instant.now());
        Student saved = students.save(updated);
        audit(id, actor, HistoryAction.STUDENT_STATUS_CHANGED, "STUDENT", id, "Estado cambiado a " + status + ".");
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDto resume(UUID studentId) {
        return new ResumeDto(get(studentId), listDocuments(studentId, null), history(studentId));
    }

    @Override
    public DocumentDto attachDocument(UUID studentId, DocumentCommand command, ActorCommand actor) {
        getDomain(studentId);
        validateDocument(command);
        FileStoragePort.StoredFile file = storage.storeDocument(command.file());
        Instant now = Instant.now();
        StudentDocument document = new StudentDocument(UUID.randomUUID(), studentId, clean(command.documentType()), clean(command.displayName()),
                safeFilename(command.file().getOriginalFilename()), file.storageKey(), file.contentType(), file.size(), clean(command.description()),
                DocumentStatus.ACTIVE, actor.userId(), actor.name(), now, now);
        StudentDocument saved = documents.save(document);
        audit(studentId, actor, HistoryAction.DOCUMENT_UPLOADED, "DOCUMENT", saved.id(), "Documento adjuntado: " + saved.displayName() + ".");
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> listDocuments(UUID studentId, String type) {
        getDomain(studentId);
        return documents.findActiveByStudentId(studentId, type).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FileResourceDto downloadDocument(UUID studentId, UUID documentId) {
        StudentDocument document = document(studentId, documentId);
        if (document.status() != DocumentStatus.ACTIVE) {
            throw new NotFoundException("El documento no existe.");
        }
        return storage.loadDocument(document.storageKey(), document.contentType(), document.originalName());
    }

    @Override
    public DocumentDto replaceDocument(UUID studentId, UUID documentId, DocumentCommand command, ActorCommand actor) {
        StudentDocument current = document(studentId, documentId);
        validateDocument(command);
        StudentDocument replaced = new StudentDocument(current.id(), current.studentId(), current.documentType(), current.displayName(), current.originalName(),
                current.storageKey(), current.contentType(), current.size(), current.description(), DocumentStatus.REPLACED, current.uploadedByUserId(),
                current.uploadedByName(), current.createdAt(), Instant.now());
        documents.save(replaced);
        DocumentDto saved = attachDocument(studentId, command, actor);
        audit(studentId, actor, HistoryAction.DOCUMENT_REPLACED, "DOCUMENT", documentId, "Documento reemplazado.");
        return saved;
    }

    @Override
    public void deleteDocument(UUID studentId, UUID documentId, ActorCommand actor) {
        if (!actor.isAdmin()) {
            throw new ForbiddenException("Solo Junta Administrativa puede eliminar documentos.");
        }
        StudentDocument current = document(studentId, documentId);
        documents.save(new StudentDocument(current.id(), current.studentId(), current.documentType(), current.displayName(), current.originalName(),
                current.storageKey(), current.contentType(), current.size(), current.description(), DocumentStatus.DELETED, current.uploadedByUserId(),
                current.uploadedByName(), current.createdAt(), Instant.now()));
        audit(studentId, actor, HistoryAction.DOCUMENT_DELETED, "DOCUMENT", documentId, "Documento eliminado: " + current.displayName() + ".");
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryEventDto> history(UUID studentId) {
        getDomain(studentId);
        return history.findByStudentId(studentId).stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FileResourceDto photo(UUID studentId) {
        Student student = getDomain(studentId);
        if (student.photoStorageKey() == null) {
            throw new NotFoundException("El estudiante no tiene fotografia.");
        }
        return storage.loadPhoto(student.photoStorageKey(), student.photoContentType());
    }

    private Student getDomain(UUID id) {
        return students.findById(id).orElseThrow(() -> new NotFoundException("El estudiante no existe."));
    }

    private StudentDocument document(UUID studentId, UUID documentId) {
        getDomain(studentId);
        return documents.findByIdAndStudentId(documentId, studentId).orElseThrow(() -> new NotFoundException("El documento no existe."));
    }

    private void validate(StudentCommand command) {
        require(command.firstName(), "Los nombres son obligatorios.");
        require(command.lastName(), "Los apellidos son obligatorios.");
        if (!clean(command.firstName()).matches(NAME_PATTERN) || clean(command.firstName()).length() < 2 || clean(command.firstName()).length() > 80) {
            throw new BadRequestException("Los nombres deben tener entre 2 y 80 caracteres validos.");
        }
        if (!clean(command.lastName()).matches(NAME_PATTERN) || clean(command.lastName()).length() < 2 || clean(command.lastName()).length() > 120) {
            throw new BadRequestException("Los apellidos deben tener entre 2 y 120 caracteres validos.");
        }
        if (command.documentNumber() == null || !command.documentNumber().matches("\\d{6,12}")) {
            throw new BadRequestException("La cedula debe tener entre 6 y 12 digitos.");
        }
        if (command.birthDate() == null || command.birthDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de nacimiento es obligatoria y no puede ser futura.");
        }
        if (command.email() != null && !command.email().isBlank() && (command.email().length() > 150 || !command.email().contains("@"))) {
            throw new BadRequestException("El correo electronico no es valido.");
        }
        if (command.phone() != null && !command.phone().isBlank() && !command.phone().matches("[0-9+]{1,20}")) {
            throw new BadRequestException("El telefono solo puede contener numeros y el simbolo +.");
        }
    }

    private void validateDocument(DocumentCommand command) {
        require(command.documentType(), "El tipo de documento es obligatorio.");
        require(command.displayName(), "El nombre visible es obligatorio.");
        if (command.displayName().length() > 140) {
            throw new BadRequestException("El nombre visible no puede superar 140 caracteres.");
        }
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "documento";
        }
        return filename.replace("\\", "_").replace("/", "_");
    }

    private void audit(UUID studentId, ActorCommand actor, HistoryAction action, String entityType, UUID entityId, String summary) {
        history.save(new HistoryEvent(UUID.randomUUID(), studentId, actor.userId(), actor.name(), action, entityType, entityId, summary, Instant.now()));
    }

    private StudentDto toDto(Student student) {
        String photoUrl = student.photoStorageKey() == null ? null : "/api/students/" + student.id() + "/photo";
        return new StudentDto(student.id(), student.firstName(), student.lastName(), student.documentNumber(), student.birthDate(), student.birthPlace(),
                student.address(), student.phone(), student.email(), photoUrl, student.status(), student.createdAt(), student.updatedAt());
    }

    private DocumentDto toDto(StudentDocument document) {
        return new DocumentDto(document.id(), document.studentId(), document.documentType(), document.displayName(), document.originalName(), document.contentType(),
                document.size(), document.description(), document.status(), document.uploadedByUserId(), document.uploadedByName(), document.createdAt(), document.updatedAt());
    }

    private HistoryEventDto toDto(HistoryEvent event) {
        return new HistoryEventDto(event.id(), event.studentId(), event.actorUserId(), event.actorName(), event.action(), event.entityType(), event.entityId(), event.summary(), event.createdAt());
    }
}
