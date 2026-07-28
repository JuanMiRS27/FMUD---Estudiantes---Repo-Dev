package com.fmud.student.domain.history;

import java.time.Instant;
import java.util.UUID;

public record HistoryEvent(
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
