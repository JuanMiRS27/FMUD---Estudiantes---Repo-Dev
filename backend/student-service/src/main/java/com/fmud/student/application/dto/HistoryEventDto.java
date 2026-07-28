package com.fmud.student.application.dto;

import com.fmud.student.domain.history.HistoryAction;

import java.time.Instant;
import java.util.UUID;

public record HistoryEventDto(
        UUID id,
        UUID studentId,
        UUID actorUserId,
        String actorName,
        HistoryAction action,
        String entityType,
        UUID entityId,
        String summary,
        Instant createdAt
) {
}
