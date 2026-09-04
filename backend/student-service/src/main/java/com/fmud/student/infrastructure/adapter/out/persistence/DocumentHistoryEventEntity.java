package com.fmud.student.infrastructure.adapter.out.persistence;

import com.fmud.student.domain.history.HistoryAction;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_history_events", schema = "students")
public class DocumentHistoryEventEntity {
    @Id
    public UUID id;
    @Column(nullable = false)
    public UUID documentId;
    @Column(nullable = false)
    public UUID actorUserId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    public HistoryAction action;
    @Column(nullable = false, length = 500)
    public String summary;
    @Column(nullable = false)
    public Instant createdAt;
}
