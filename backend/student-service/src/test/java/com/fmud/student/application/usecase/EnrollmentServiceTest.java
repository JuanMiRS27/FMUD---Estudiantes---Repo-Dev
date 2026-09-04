package com.fmud.student.application.usecase;

import com.fmud.student.application.command.EnrollmentCommand;
import com.fmud.student.application.port.out.EnrollmentRepositoryPort;
import com.fmud.student.application.port.out.StudentRepositoryPort;
import com.fmud.student.domain.enrollment.Enrollment;
import com.fmud.student.domain.enrollment.EnrollmentStatus;
import com.fmud.student.domain.model.Student;
import com.fmud.student.domain.model.StudentStatus;
import com.fmud.student.infrastructure.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnrollmentServiceTest {
    private final InMemoryStudents students = new InMemoryStudents();
    private final InMemoryEnrollments enrollments = new InMemoryEnrollments();
    private final EnrollmentService service = new EnrollmentService(enrollments, students);
    private final UUID studentId = UUID.randomUUID();

    @Test
    void createsListsAndUpdatesEnrollment() {
        students.save(student());

        var created = service.create(studentId, new EnrollmentCommand("2026-1", "Programa Infantil", EnrollmentStatus.ENROLLED));
        var updated = service.update(studentId, created.id(), new EnrollmentCommand("2026-1", "Programa Juvenil", EnrollmentStatus.COMPLETED));

        assertThat(service.list(studentId)).hasSize(1);
        assertThat(updated.program()).isEqualTo("Programa Juvenil");
        assertThat(updated.status()).isEqualTo(EnrollmentStatus.COMPLETED);
    }

    @Test
    void rejectsDuplicatePeriodForSameStudent() {
        students.save(student());

        service.create(studentId, new EnrollmentCommand("2026-1", "Programa Infantil", EnrollmentStatus.ENROLLED));

        assertThatThrownBy(() -> service.create(studentId, new EnrollmentCommand("2026-1", "Otro programa", EnrollmentStatus.ENROLLED)))
                .isInstanceOf(BadRequestException.class);
    }

    private Student student() {
        Instant now = Instant.now();
        return new Student(studentId, "Ana", "Perez", "12345678", LocalDate.of(2012, 3, 4),
                "Bogota", "Calle 1", "+573001112233", "ana@example.com", StudentStatus.ACTIVE, now, now);
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

    private static final class InMemoryEnrollments implements EnrollmentRepositoryPort {
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
}
