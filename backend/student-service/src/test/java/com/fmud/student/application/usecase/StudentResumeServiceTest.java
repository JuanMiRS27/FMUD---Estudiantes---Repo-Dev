package com.fmud.student.application.usecase;

import com.fmud.student.application.command.ActorCommand;
import com.fmud.student.application.command.DocumentCommand;
import com.fmud.student.application.command.StudentCommand;
import com.fmud.student.application.dto.FileResourceDto;
import com.fmud.student.application.dto.ResumeDetailsDto;
import com.fmud.student.application.port.out.DocumentRepositoryPort;
import com.fmud.student.application.port.out.FileStoragePort;
import com.fmud.student.application.port.out.HistoryRepositoryPort;
import com.fmud.student.application.port.out.ResumeDetailsRepositoryPort;
import com.fmud.student.application.port.out.StudentRepositoryPort;
import com.fmud.student.domain.document.DocumentStatus;
import com.fmud.student.domain.document.StudentDocument;
import com.fmud.student.domain.enrollment.Enrollment;
import com.fmud.student.domain.history.DocumentHistoryEvent;
import com.fmud.student.domain.history.StudentHistoryEvent;
import com.fmud.student.domain.model.Student;
import com.fmud.student.domain.model.StudentStatus;
import com.fmud.student.infrastructure.exception.BadRequestException;
import com.fmud.student.infrastructure.exception.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentResumeServiceTest {
    private final InMemoryStudents students = new InMemoryStudents();
    private final InMemoryDocuments documents = new InMemoryDocuments();
    private final InMemoryHistory history = new InMemoryHistory();
    private final InMemoryEnrollments enrollments = new InMemoryEnrollments();
    private final InMemoryResumeDetails details = new InMemoryResumeDetails();
    private final FakeStorage storage = new FakeStorage();
    private final StudentResumeService service = new StudentResumeService(students, documents, history, enrollments, details, storage);
    private final ActorCommand admin = new ActorCommand(UUID.randomUUID(), "Admin", "ADMIN");
    private final ActorCommand secretary = new ActorCommand(UUID.randomUUID(), "Secretaria", "SECRETARIO");

    @Test
    void createsStudentAndAuditsCreation() {
        var saved = service.create(validStudent("12345678"), admin);

        assertThat(saved.documentNumber()).isEqualTo("12345678");
        assertThat(service.list("", null, Pageable.unpaged()).totalElements()).isEqualTo(1);
        assertThat(service.history(saved.id())).extracting("summary").contains("Estudiante creado.");
    }

    @Test
    void rejectsDuplicateDocumentNumberAndInvalidBirthDate() {
        service.create(validStudent("12345678"), admin);

        assertThatThrownBy(() -> service.create(validStudent("12345678"), admin)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.create(new StudentCommand("Ana", "Perez", "87654321", LocalDate.now().plusDays(1),
                "", "", "", "", StudentStatus.ACTIVE, null), admin)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void updatesStudentWithPhoneFormatAllowedByFrontend() {
        var student = service.create(validStudent("12345678"), admin);

        var updated = service.update(student.id(), new StudentCommand("Ana Maria", "Perez Gomez", "12345678", LocalDate.of(2012, 3, 4),
                "Bogota", "Calle 1", "+57 300 111 2233", "ana@example.com", StudentStatus.ACTIVE, null), secretary);

        assertThat(updated.phone()).isEqualTo("+57 300 111 2233");
        assertThat(service.history(student.id())).extracting("summary").contains("Informacion del estudiante actualizada.");
    }

    @Test
    void savesAndReadsCompleteResumeDetails() {
        var student = service.create(validStudent("12345678"), admin);
        var detail = new ResumeDetailsDto(
                Map.of("documentType", "Cedula de ciudadania", "municipality", "Bogota"),
                Map.of("currentlyWorks", "Si", "company", "Fundacion"),
                Map.of("internetAccess", "Si"),
                Map.of("scholarshipReason", "Proyecto de vida"),
                Map.of("availableForClasses", "Si"),
                Map.of("knewFoundationBefore", "No"),
                Map.of("personalDataProcessing", "Si"),
                Map.of("diagnosedDisease", "No"),
                Map.of("tobaccoUse", "No"),
                Map.of("strengthAreas", List.of("Matematicas")),
                Map.of("nursingUnderstanding", "Cuidado humanizado"),
                Map.of("studentRulesCommitment", "Si"),
                Map.of("truthfulCompleteInformation", "Si")
        );

        service.updateDetails(student.id(), detail, secretary);
        var resume = service.resume(student.id());

        assertThat(resume.details().personal()).containsEntry("documentType", "Cedula de ciudadania");
        assertThat(resume.details().socioeconomic()).containsEntry("company", "Fundacion");
        assertThat(resume.details().academicPerformance()).containsEntry("strengthAreas", List.of("Matematicas"));
        assertThat(service.history(student.id())).extracting("summary").contains("Detalle completo de hoja de vida actualizado.");
    }

    @Test
    void secretaryCannotDeleteDocumentButAdminCan() {
        var student = service.create(validStudent("12345678"), admin);
        var file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "abc".getBytes());
        var document = service.attachDocument(student.id(), new DocumentCommand("IDENTITY_DOCUMENT", "Cedula", "", file), secretary);

        assertThatThrownBy(() -> service.deleteDocument(student.id(), document.id(), secretary)).isInstanceOf(ForbiddenException.class);

        service.deleteDocument(student.id(), document.id(), admin);
        assertThat(documents.rows.get(document.id()).status()).isEqualTo(DocumentStatus.DELETED);
    }

    private StudentCommand validStudent(String document) {
        return new StudentCommand("Ana Maria", "Perez Gomez", document, LocalDate.of(2012, 3, 4),
                "Bogota", "Calle 1", "+573001112233", "ana@example.com", StudentStatus.ACTIVE, null);
    }

    private static final class InMemoryStudents implements StudentRepositoryPort {
        private final Map<UUID, Student> rows = new LinkedHashMap<>();

        public Student save(Student student) {
            rows.put(student.id(), student);
            return student;
        }

        public Optional<Student> findById(UUID id) {
            return Optional.ofNullable(rows.get(id));
        }

        public boolean existsByDocumentNumber(String documentNumber) {
            return rows.values().stream().anyMatch(student -> student.documentNumber().equals(documentNumber));
        }

        public boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id) {
            return rows.values().stream().anyMatch(student -> student.documentNumber().equals(documentNumber) && !student.id().equals(id));
        }

        public Page<Student> search(String search, StudentStatus status, Pageable pageable) {
            return new PageImpl<>(rows.values().stream().toList());
        }
    }

    private static final class InMemoryDocuments implements DocumentRepositoryPort {
        private final Map<UUID, StudentDocument> rows = new LinkedHashMap<>();

        public StudentDocument save(StudentDocument document) {
            rows.put(document.id(), document);
            return document;
        }

        public Optional<StudentDocument> findByIdAndStudentId(UUID id, UUID studentId) {
            return Optional.ofNullable(rows.get(id)).filter(document -> document.studentId().equals(studentId));
        }

        public Optional<StudentDocument> findActiveByStudentIdAndType(UUID studentId, String type) {
            return rows.values().stream()
                    .filter(document -> document.studentId().equals(studentId) && document.status() == DocumentStatus.ACTIVE && document.documentType().equals(type))
                    .findFirst();
        }

        public List<StudentDocument> findActiveByStudentId(UUID studentId, String type) {
            return rows.values().stream()
                    .filter(document -> document.studentId().equals(studentId) && document.status() == DocumentStatus.ACTIVE)
                    .filter(document -> type == null || type.isBlank() || document.documentType().equals(type))
                    .toList();
        }
    }

    private static final class InMemoryHistory implements HistoryRepositoryPort {
        private final List<StudentHistoryEvent> studentRows = new ArrayList<>();
        private final List<DocumentHistoryEvent> documentRows = new ArrayList<>();

        public StudentHistoryEvent saveStudent(StudentHistoryEvent event) {
            studentRows.add(event);
            return event;
        }

        public DocumentHistoryEvent saveDocument(DocumentHistoryEvent event) {
            documentRows.add(event);
            return event;
        }

        public List<StudentHistoryEvent> findStudentEventsByStudentId(UUID studentId) {
            return studentRows.stream().filter(event -> event.studentId().equals(studentId)).toList();
        }

        public List<DocumentHistoryEvent> findDocumentEventsByStudentId(UUID studentId) {
            return documentRows;
        }
    }

    private static final class InMemoryEnrollments implements com.fmud.student.application.port.out.EnrollmentRepositoryPort {
        private final Map<UUID, Enrollment> rows = new LinkedHashMap<>();

        public Enrollment save(Enrollment enrollment) {
            rows.put(enrollment.id(), enrollment);
            return enrollment;
        }

        public Optional<Enrollment> findByIdAndStudentId(UUID id, UUID studentId) {
            return Optional.ofNullable(rows.get(id)).filter(enrollment -> enrollment.studentId().equals(studentId));
        }

        public List<Enrollment> findByStudentId(UUID studentId) {
            return rows.values().stream().filter(enrollment -> enrollment.studentId().equals(studentId)).toList();
        }

        public boolean existsByStudentIdAndPeriodCode(UUID studentId, String periodCode) {
            return rows.values().stream().anyMatch(enrollment -> enrollment.studentId().equals(studentId) && enrollment.periodCode().equals(periodCode));
        }

        public boolean existsByStudentIdAndPeriodCodeAndIdNot(UUID studentId, String periodCode, UUID id) {
            return rows.values().stream().anyMatch(enrollment -> enrollment.studentId().equals(studentId)
                    && enrollment.periodCode().equals(periodCode)
                    && !enrollment.id().equals(id));
        }
    }

    private static final class InMemoryResumeDetails implements ResumeDetailsRepositoryPort {
        private final Map<UUID, ResumeDetailsDto> rows = new LinkedHashMap<>();

        public ResumeDetailsDto findByStudentId(UUID studentId) {
            return rows.getOrDefault(studentId, empty());
        }

        public void save(UUID studentId, ResumeDetailsDto details) {
            rows.put(studentId, details);
        }

        private ResumeDetailsDto empty() {
            return new ResumeDetailsDto(Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                    Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }
    }

    private static final class FakeStorage implements FileStoragePort {
        public StoredFile storePhoto(org.springframework.web.multipart.MultipartFile file) {
            return new StoredFile("photo.jpg", "image/jpeg", file.getSize());
        }

        public StoredFile storeDocument(org.springframework.web.multipart.MultipartFile file) {
            if (!"application/pdf".equals(file.getContentType())) {
                throw new BadRequestException("Formato de documento no permitido.");
            }
            return new StoredFile("doc.pdf", "application/pdf", file.getSize());
        }

        public FileResourceDto loadPhoto(String storageKey, String contentType) {
            return new FileResourceDto(new ByteArrayResource(new byte[0]), contentType, "photo", 0);
        }

        public FileResourceDto loadDocument(String storageKey, String contentType, String filename) {
            return new FileResourceDto(new ByteArrayResource("abc".getBytes()), contentType, filename, 3);
        }
    }
}
