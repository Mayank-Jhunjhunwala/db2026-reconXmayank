package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "dlq_messages")
public class DlqMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, name = "event_id")
    private String eventId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT", name = "exception_message")
    private String exceptionMessage;

    @Column(nullable = false, name = "created_at")
    private Instant createdAt = Instant.now();

    public DlqMessage() {}

    public DlqMessage(String topic, String eventId, String payload, String exceptionMessage) {
        this.topic = topic;
        this.eventId = eventId;
        this.payload = payload;
        this.exceptionMessage = exceptionMessage;
    }

    public Long getId() { return id; }
    public String getTopic() { return topic; }
    public String getEventId() { return eventId; }
    public String getPayload() { return payload; }
    public String getExceptionMessage() { return exceptionMessage; }
    public Instant getCreatedAt() { return createdAt; }
}
