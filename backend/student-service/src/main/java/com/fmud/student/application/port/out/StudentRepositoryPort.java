package com.fmud.student.application.port.out;

import com.fmud.student.domain.model.Student;
import com.fmud.student.domain.model.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepositoryPort {
    Student save(Student student);

    Optional<Student> findById(UUID id);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);

    Page<Student> search(String search, StudentStatus status, Pageable pageable);
}
