package com.infnet.tp3.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityName;

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, STOCK_CHANGE, STATUS_CHANGE, SHIPPING_DISPATCH, DELETE

    @Column(length = 1000)
    private String detailDescription;

    @Column(length = 500)
    private String previousValue;

    @Column(length = 500)
    private String newValue;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
