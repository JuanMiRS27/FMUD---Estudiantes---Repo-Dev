package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.domain.document.DocumentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_documents")
public class StudentDocumentEntity {
    @Id
    public UUID id;
    @Column(nullable = false)
    public UUID studentId;
    @Column(nullable = false)
    public String documentType;
    @Column(nullable = false)
    public String displayName;
    @Column(nullable = false)
    public String originalName;
    @Column(nullable = false)
    public String storageKey;
    @Column(nullable = false)
    public String contentType;
    @Column(nullable = false)
    public long sizeBytes;
    public String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public DocumentStatus status;
    public UUID uploadedByUserId;
    @Column(nullable = false)
    public String uploadedByName;
    @Column(nullable = false)
    public Instant createdAt;
    @Column(nullable = false)
    public Instant updatedAt;
}
