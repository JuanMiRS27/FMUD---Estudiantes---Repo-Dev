package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.domain.model.StudentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "students", schema = "students")
public class StudentEntity {
    @Id
    public UUID id;
    @Column(nullable = false, length = 80)
    public String firstName;
    @Column(nullable = false, length = 120)
    public String lastName;
    @Column(nullable = false, unique = true, length = 12)
    public String documentNumber;
    @Column(nullable = false)
    public LocalDate birthDate;
    public String birthPlace;
    public String address;
    public String phone;
    public String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StudentStatus status;
    @Column(nullable = false)
    public Instant createdAt;
    @Column(nullable = false)
    public Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
