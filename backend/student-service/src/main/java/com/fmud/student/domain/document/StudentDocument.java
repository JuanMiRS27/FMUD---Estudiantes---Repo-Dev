package com.fmud.student.domain.document;

import java.time.Instant;
import java.util.UUID;

public record StudentDocument(
        UUID id,
        UUID studentId,
        String documentType,
        String displayName,
        String originalName,
        String storageKey,
        String contentType,
        long size,
        String description,
        DocumentStatus status,
        UUID uploadedByUserId,
        Instant createdAt,
        Instant updatedAt
) {
}
