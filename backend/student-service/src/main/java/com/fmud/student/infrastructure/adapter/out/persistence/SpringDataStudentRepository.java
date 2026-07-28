package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.domain.model.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

interface SpringDataStudentRepository extends JpaRepository<StudentEntity, UUID> {
    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumberAndIdNot(String documentNumber, UUID id);

    @Query("""
            select s from StudentEntity s
            where (:status is null or s.status = :status)
              and (:search is null or :search = ''
                or lower(s.firstName) like lower(concat('%', :search, '%'))
                or lower(s.lastName) like lower(concat('%', :search, '%'))
                or s.documentNumber like concat('%', :search, '%'))
            """)
    Page<StudentEntity> search(String search, StudentStatus status, Pageable pageable);
}
