package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.ExcursionSeverity;
import com.pharmacold.backend.domain.enums.ExcursionStatus;
import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import org.apache.commons.lang3.compare.ComparableUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.function.Predicate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "thermal_excursions")
public class ThermalExcursion extends BaseAuditableEntity {

    private static final BigDecimal CELSIUS_THRESHOLD_TOLERANCE = BigDecimal.valueOf(5.00);

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

    public static ThermalExcursion createRecord(BigDecimal recordedTemperature, Instant detectedAt, Shipment shipment) {
        Predicate<BigDecimal> exceedsThresholdMinTemperature =
                ComparableUtils.le(shipment.getMinTempCelsius().subtract(CELSIUS_THRESHOLD_TOLERANCE));
        Predicate<BigDecimal> exceedsThresholdMaxTemperature =
                ComparableUtils.ge(shipment.getMaxTempCelsius().add(CELSIUS_THRESHOLD_TOLERANCE));
        boolean exceedsThreshold = exceedsThresholdMinTemperature.or(exceedsThresholdMaxTemperature)
                .test(recordedTemperature);
        return ThermalExcursion.builder()
                .recordedTemperature(recordedTemperature)
                .compromisedShipment(shipment)
                .status(ExcursionStatus.OPEN)
                .severity(exceedsThreshold ? ExcursionSeverity.CRITICAL : ExcursionSeverity.WARNING)
                .detectedAt(detectedAt)
                .build();
    }

    public boolean isCritical() {
        return ExcursionSeverity.CRITICAL == this.getSeverity();
    }

    public ThermalExcursionDetectedEvent toThermalExcursionDetectedEvent() {
        return ThermalExcursionDetectedEvent.builder()
                .detectedAt(this.detectedAt)
                .recordedTemperature(this.recordedTemperature)
                .severity(this.severity)
                .trackingNumber(this.compromisedShipment.getTrackingNumber())
                .minTempCelsius(this.compromisedShipment.getMinTempCelsius())
                .maxTempCelsius(this.compromisedShipment.getMaxTempCelsius())
                .build();
    }
}
