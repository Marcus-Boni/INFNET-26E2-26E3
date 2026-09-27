package com.infnet.shipping.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false, unique = true, length = 50)
    private String trackingNumber;

    @Column(nullable = false)
    private String carrier;

    @Column(nullable = false)
    private String serviceType; // EXPRESS, STANDARD, ECONOMICAL

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal freightCost;

    @Column(nullable = false)
    private Integer estimatedDeliveryDays;

    @Column(nullable = false)
    private String destinationStreet;

    @Column(nullable = false)
    private String destinationCity;

    @Column(nullable = false, length = 2)
    private String destinationState;

    @Column(nullable = false, length = 20)
    private String destinationZipCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime lastModifiedAt;

    @Transient
    @Builder.Default
    private List<TrackingEvent> events = new ArrayList<>();
}
