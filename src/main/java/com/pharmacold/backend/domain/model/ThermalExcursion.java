package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.ExcursionSeverity;
import com.pharmacold.backend.domain.enums.ExcursionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;

import java.math.BigDecimal;
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

    @Digits(integer = 3, fraction = 2, message = "Recorded temperature must have at most 3 integer and 2 decimal places")
    @Column(name = "recorded_temperature", nullable = false)
    private BigDecimal recordedTemperature;

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
