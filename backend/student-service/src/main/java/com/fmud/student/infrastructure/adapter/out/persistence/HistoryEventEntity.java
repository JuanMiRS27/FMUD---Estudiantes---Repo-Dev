package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.domain.history.HistoryAction;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "student_history_events")
public class HistoryEventEntity {
    @Id
    public UUID id;
    @Column(nullable = false)
    public UUID studentId;
    public UUID actorUserId;
    @Column(nullable = false)
    public String actorName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public HistoryAction action;
    @Column(nullable = false)
    public String entityType;
    @Column(nullable = false)
    public UUID entityId;
    @Column(nullable = false)
    public String summary;
    @Column(nullable = false)
    public Instant createdAt;
}
