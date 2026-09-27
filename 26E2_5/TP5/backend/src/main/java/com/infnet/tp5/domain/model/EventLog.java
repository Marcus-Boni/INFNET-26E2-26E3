package com.infnet.tp5.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_logs", indexes = {
        @Index(name = "idx_event_id", columnList = "event_id"),
        @Index(name = "idx_correlation_id", columnList = "correlation_id"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "exchange")
    private String exchange;

    @Column(name = "routing_key")
    private String routingKey;

    @Column(name = "direction", nullable = false)
    private String direction; // PUBLISHED, RECEIVED

    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILED, DLQ, RETRY

    @Lob
    @Column(name = "payload", columnDefinition = "CLOB")
    private String payload;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}

