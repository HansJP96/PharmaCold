package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.ExcursionSeverity;
import com.pharmacold.backend.domain.enums.ExcursionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "thermal_excursions")
public class ThermalExcursion extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "recorded_temperature", nullable = false)
    private Double recordedTemperature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExcursionSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExcursionStatus status;

    @Column(name = "root_cause_analysis")
    private String rootCauseAnalysis;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment compromisedShipment;
}
