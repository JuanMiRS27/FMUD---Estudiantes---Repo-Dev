package com.fmud.student.application.usecase;

import com.fmud.student.application.command.ActorCommand;
import com.fmud.student.application.command.DocumentCommand;
import com.fmud.student.application.command.StudentCommand;
import com.fmud.student.application.dto.*;
import com.fmud.student.application.port.in.StudentResumeUseCase;
import com.fmud.student.application.port.out.DocumentRepositoryPort;
import com.fmud.student.application.port.out.FileStoragePort;
import com.fmud.student.application.port.out.HistoryRepositoryPort;
import com.fmud.student.application.port.out.ResumeDetailsRepositoryPort;
import com.fmud.student.application.port.out.StudentRepositoryPort;
import com.fmud.student.domain.document.DocumentStatus;
import com.fmud.student.domain.document.StudentDocument;
import com.fmud.student.domain.history.DocumentHistoryEvent;
import com.fmud.student.domain.history.HistoryAction;
import com.fmud.student.domain.history.StudentHistoryEvent;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StudentResumeService implements StudentResumeUseCase {
    private static final String NAME_PATTERN = "^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ -]+$";
    private static final String PHONE_PATTERN = "^\\+?[0-9](?:[0-9 ]{4,18}[0-9])?$";
    private static final List<String> SINGLETON_DOCUMENT_TYPES = List.of("PHOTO", "SIGNATURE");
    private final StudentRepositoryPort students;
    private final DocumentRepositoryPort documents;
    private final HistoryRepositoryPort history;
    private final com.fmud.student.application.port.out.EnrollmentRepositoryPort enrollments;
    private final ResumeDetailsRepositoryPort details;
    private final FileStoragePort storage;

    public StudentResumeService(StudentRepositoryPort students, DocumentRepositoryPort documents, HistoryRepositoryPort history,
                                com.fmud.student.application.port.out.EnrollmentRepositoryPort enrollments, ResumeDetailsRepositoryPort details,
                                FileStoragePort storage) {
        this.students = students;
        this.documents = documents;
        this.history = history;
        this.enrollments = enrollments;
        this.details = details;
        this.storage = storage;
    }

    @Override
    public StudentDto create(StudentCommand command, ActorCommand actor) {
        validate(command);
        if (students.existsByDocumentNumber(command.documentNumber())) {
            throw new BadRequestException("La cedula ya se encuentra registrada.");
        }
        Instant now = Instant.now();
        Student student = new Student(UUID.randomUUID(), clean(command.firstName()), clean(command.lastName()), command.documentNumber(),
                command.birthDate(), clean(command.birthPlace()), clean(command.address()), clean(command.phone()), clean(command.email()),
                command.status() == null ? StudentStatus.ACTIVE : command.status(), now, now);
        Student saved = students.save(student);
        auditStudent(saved.id(), actor, HistoryAction.STUDENT_CREATED, "Estudiante creado.");
        if (command.photo() != null && !command.photo().isEmpty()) {
            savePhotoDocument(saved.id(), storage.storePhoto(command.photo()), actor, now, "Fotografia cargada.");
        }
        return toDto(saved);
    }

    @Override
    public StudentDto update(UUID id, StudentCommand command, ActorCommand actor) {
        Student current = getDomain(id);
        validate(command);
        if (students.existsByDocumentNumberAndIdNot(command.documentNumber(), id)) {
            throw new BadRequestException("La cedula ya se encuentra registrada.");
        }
        Student updated = new Student(current.id(), clean(command.firstName()), clean(command.lastName()), command.documentNumber(), command.birthDate(),
                clean(command.birthPlace()), clean(command.address()), clean(command.phone()), clean(command.email()),
                command.status() == null ? current.status() : command.status(), current.createdAt(), Instant.now());
        Student saved = students.save(updated);
        auditStudent(id, actor, HistoryAction.STUDENT_UPDATED, "Informacion del estudiante actualizada.");
        if (command.photo() != null && !command.photo().isEmpty()) {
            replaceActiveSingleton(id, "PHOTO", actor);
            savePhotoDocument(id, storage.storePhoto(command.photo()), actor, Instant.now(), "Fotografia actualizada.");
        }
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
                current.address(), current.phone(), current.email(), status, current.createdAt(), Instant.now());
        Student saved = students.save(updated);
        auditStudent(id, actor, HistoryAction.STUDENT_STATUS_CHANGED, "Estado cambiado a " + status + ".");
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeDto resume(UUID studentId) {
        return new ResumeDto(get(studentId), details.findByStudentId(studentId), enrollments.findByStudentId(studentId).stream()
                .map(enrollment -> new EnrollmentDto(enrollment.id(), enrollment.studentId(), enrollment.periodCode(), enrollment.program(),
                        enrollment.status(), enrollment.createdAt(), enrollment.updatedAt()))
                .toList(), listDocuments(studentId, null), history(studentId));
    }

    @Override
    public ResumeDetailsDto updateDetails(UUID studentId, ResumeDetailsDto detail, ActorCommand actor) {
        getDomain(studentId);
        details.save(studentId, detail);
        auditStudent(studentId, actor, HistoryAction.STUDENT_UPDATED, "Detalle completo de hoja de vida actualizado.");
        return details.findByStudentId(studentId);
    }

    @Override
    public DocumentDto attachDocument(UUID studentId, DocumentCommand command, ActorCommand actor) {
        getDomain(studentId);
        validateDocument(command);
        String documentType = clean(command.documentType());
        if (SINGLETON_DOCUMENT_TYPES.contains(documentType)) {
            replaceActiveSingleton(studentId, documentType, actor);
        }
        FileStoragePort.StoredFile file = "PHOTO".equals(documentType) ? storage.storePhoto(command.file()) : storage.storeDocument(command.file());
        Instant now = Instant.now();
        StudentDocument document = new StudentDocument(UUID.randomUUID(), studentId, documentType, clean(command.displayName()),
                safeFilename(command.file().getOriginalFilename()), file.storageKey(), file.contentType(), file.size(), clean(command.description()),
                DocumentStatus.ACTIVE, actor.userId(), now, now);
        StudentDocument saved = documents.save(document);
        auditDocument(saved.id(), actor, HistoryAction.DOCUMENT_UPLOADED, "Documento adjuntado: " + saved.displayName() + ".");
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
                current.createdAt(), Instant.now());
        documents.save(replaced);
        String documentType = clean(command.documentType());
        if (SINGLETON_DOCUMENT_TYPES.contains(documentType)) {
            replaceActiveSingleton(studentId, documentType, actor);
        }
        FileStoragePort.StoredFile file = "PHOTO".equals(documentType) ? storage.storePhoto(command.file()) : storage.storeDocument(command.file());
        Instant now = Instant.now();
        StudentDocument document = new StudentDocument(UUID.randomUUID(), studentId, documentType, clean(command.displayName()),
                safeFilename(command.file().getOriginalFilename()), file.storageKey(), file.contentType(), file.size(), clean(command.description()),
                DocumentStatus.ACTIVE, actor.userId(), now, now);
        StudentDocument saved = documents.save(document);
        auditDocument(documentId, actor, HistoryAction.DOCUMENT_REPLACED, "Documento reemplazado.");
        return toDto(saved);
    }

    @Override
    public void deleteDocument(UUID studentId, UUID documentId, ActorCommand actor) {
        if (!actor.isAdmin()) {
            throw new ForbiddenException("Solo Junta Administrativa puede eliminar documentos.");
        }
        StudentDocument current = document(studentId, documentId);
        documents.save(new StudentDocument(current.id(), current.studentId(), current.documentType(), current.displayName(), current.originalName(),
                current.storageKey(), current.contentType(), current.size(), current.description(), DocumentStatus.DELETED, current.uploadedByUserId(),
                current.createdAt(), Instant.now()));
        auditDocument(documentId, actor, HistoryAction.DOCUMENT_DELETED, "Documento eliminado: " + current.displayName() + ".");
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoryEventDto> history(UUID studentId) {
        getDomain(studentId);
        List<HistoryEventDto> studentEvents = history.findStudentEventsByStudentId(studentId).stream().map(this::toDto).toList();
        List<HistoryEventDto> documentEvents = history.findDocumentEventsByStudentId(studentId).stream().map(event -> toDto(studentId, event)).toList();
        return java.util.stream.Stream.concat(studentEvents.stream(), documentEvents.stream())
                .sorted(Comparator.comparing(HistoryEventDto::createdAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FileResourceDto photo(UUID studentId) {
        getDomain(studentId);
        StudentDocument photo = documents.findActiveByStudentIdAndType(studentId, "PHOTO")
                .orElseThrow(() -> new NotFoundException("El estudiante no tiene fotografia."));
        return storage.loadPhoto(photo.storageKey(), photo.contentType());
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
        if (command.phone() != null && !command.phone().isBlank() && !command.phone().matches(PHONE_PATTERN)) {
            throw new BadRequestException("El telefono solo puede contener digitos, espacios controlados y el simbolo + al inicio.");
        }
    }

    private void validateDocument(DocumentCommand command) {
        require(command.documentType(), "El tipo de documento es obligatorio.");
        require(command.displayName(), "El nombre visible es obligatorio.");
        if (!List.of("PHOTO", "SIGNATURE", "IDENTITY_DOCUMENT", "CIVIL_REGISTRY", "STUDY_CERTIFICATE", "HEALTH_AFFILIATION", "SIGNED_RESUME", "OTHER")
                .contains(clean(command.documentType()))) {
            throw new BadRequestException("El tipo de documento no es valido.");
        }
        if (command.displayName().length() > 140) {
            throw new BadRequestException("El nombre visible no puede superar 140 caracteres.");
        }
        if (command.file() == null || command.file().isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio.");
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

    private void replaceActiveSingleton(UUID studentId, String documentType, ActorCommand actor) {
        documents.findActiveByStudentIdAndType(studentId, documentType).ifPresent(current -> {
            documents.save(new StudentDocument(current.id(), current.studentId(), current.documentType(), current.displayName(), current.originalName(),
                    current.storageKey(), current.contentType(), current.size(), current.description(), DocumentStatus.REPLACED, current.uploadedByUserId(),
                    current.createdAt(), Instant.now()));
            String summary = "SIGNATURE".equals(documentType) ? "Firma reemplazada." : "Fotografia reemplazada.";
            auditDocument(current.id(), actor, HistoryAction.DOCUMENT_REPLACED, summary);
        });
    }

    private StudentDocument savePhotoDocument(UUID studentId, FileStoragePort.StoredFile photo, ActorCommand actor, Instant now, String summary) {
        StudentDocument document = new StudentDocument(UUID.randomUUID(), studentId, "PHOTO", "Fotografia", "fotografia",
                photo.storageKey(), photo.contentType(), photo.size(), null, DocumentStatus.ACTIVE, actor.userId(), now, now);
        StudentDocument saved = documents.save(document);
        auditDocument(saved.id(), actor, HistoryAction.DOCUMENT_UPLOADED, summary);
        return saved;
    }

    private void auditStudent(UUID studentId, ActorCommand actor, HistoryAction action, String summary) {
        history.saveStudent(new StudentHistoryEvent(UUID.randomUUID(), studentId, actor.userId(), action, summary, Instant.now()));
    }

    private void auditDocument(UUID documentId, ActorCommand actor, HistoryAction action, String summary) {
        history.saveDocument(new DocumentHistoryEvent(UUID.randomUUID(), documentId, actor.userId(), action, summary, Instant.now()));
    }

    private StudentDto toDto(Student student) {
        String photoUrl = documents.findActiveByStudentIdAndType(student.id(), "PHOTO").isPresent() ? "/api/students/" + student.id() + "/photo" : null;
        return new StudentDto(student.id(), student.firstName(), student.lastName(), student.documentNumber(), student.birthDate(), student.birthPlace(),
                student.address(), student.phone(), student.email(), photoUrl, student.status(), student.createdAt(), student.updatedAt());
    }

    private DocumentDto toDto(StudentDocument document) {
        return new DocumentDto(document.id(), document.studentId(), document.documentType(), document.displayName(), document.originalName(), document.contentType(),
                document.size(), document.description(), document.status(), document.uploadedByUserId(), document.createdAt(), document.updatedAt());
    }

    private HistoryEventDto toDto(StudentHistoryEvent event) {
        return new HistoryEventDto(event.id(), event.studentId(), null, event.actorUserId(), event.action(), event.summary(), event.createdAt());
    }

    private HistoryEventDto toDto(UUID studentId, DocumentHistoryEvent event) {
        return new HistoryEventDto(event.id(), studentId, event.documentId(), event.actorUserId(), event.action(), event.summary(), event.createdAt());
    }
}
