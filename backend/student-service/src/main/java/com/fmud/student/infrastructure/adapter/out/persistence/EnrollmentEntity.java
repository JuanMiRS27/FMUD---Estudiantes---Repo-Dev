package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.domain.enrollment.EnrollmentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollments", schema = "students",
        uniqueConstraints = @UniqueConstraint(name = "uq_enrollments_student_period", columnNames = {"student_id", "period_code"}))
public class EnrollmentEntity {
    @Id
    public UUID id;
    @Column(nullable = false)
    public UUID studentId;
    @Column(nullable = false, length = 30)
    public String periodCode;
    @Column(nullable = false, length = 120)
    public String program;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public EnrollmentStatus status;
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
