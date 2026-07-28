package com.fmud.student.application.port.out;

import com.fmud.student.domain.history.HistoryEvent;

import java.util.List;
import java.util.UUID;

public interface HistoryRepositoryPort {
    HistoryEvent save(HistoryEvent event);

    List<HistoryEvent> findByStudentId(UUID studentId);
}
