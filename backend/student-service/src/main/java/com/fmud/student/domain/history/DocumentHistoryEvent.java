package com.fmud.student.domain.history;

import java.time.Instant;
import java.util.UUID;

public record DocumentHistoryEvent(
        UUID id,
        UUID documentId,
        UUID actorUserId,
        HistoryAction action,
        String summary,
        Instant createdAt
) {
}
