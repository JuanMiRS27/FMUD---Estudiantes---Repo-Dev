package com.fmud.student.domain.history;

import java.time.Instant;
import java.util.UUID;

public record StudentHistoryEvent(
        UUID id,
        UUID studentId,
        UUID actorUserId,
        HistoryAction action,
        String summary,
        Instant createdAt
) {
}
