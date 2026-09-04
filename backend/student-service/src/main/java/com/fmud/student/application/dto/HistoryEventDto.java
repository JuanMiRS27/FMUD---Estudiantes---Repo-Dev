package com.fmud.student.application.dto;

import com.fmud.student.domain.history.HistoryAction;

import java.time.Instant;
import java.util.UUID;

public record HistoryEventDto(
        UUID id,
        UUID studentId,
        UUID documentId,
        UUID actorUserId,
        HistoryAction action,
        String summary,
        Instant createdAt
) {
}
