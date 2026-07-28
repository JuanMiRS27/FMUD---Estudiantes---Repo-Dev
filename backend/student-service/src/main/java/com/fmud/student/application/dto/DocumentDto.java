package com.fmud.student.application.dto;

import com.fmud.student.domain.document.DocumentStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentDto(
        UUID id,
        UUID studentId,
        String documentType,
        String displayName,
        String originalName,
        String contentType,
        long size,
        String description,
        DocumentStatus status,
        UUID uploadedByUserId,
        String uploadedByName,
        Instant createdAt,
        Instant updatedAt
) {
}
