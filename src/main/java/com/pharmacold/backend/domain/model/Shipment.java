package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.ShipmentStatus;
import com.pharmacold.backend.domain.exception.InconsistentTelemetrySensorException;
import com.pharmacold.backend.domain.exception.InvalidShipmentStateException;
import com.pharmacold.backend.domain.exception.InvalidShipmentStateTransitionException;
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
@Table(name = "shipments")
public class Shipment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @Column(name = "origin_facility", nullable = false)
    private String originFacility;

    @Column(name = "destination_facility", nullable = false)
    private String destinationFacility;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Digits(integer = 3, fraction = 2, message = "Minimum temperature must have at most 3 integer and 2 decimal places")
    @Column(name = "min_temp_celsius", nullable = false)
    private BigDecimal minTempCelsius;

    @Digits(integer = 3, fraction = 2, message = "Maximum temperature must have at most 3 integer and 2 decimal places")
    @Column(name = "max_temp_celsius", nullable = false)
    private BigDecimal maxTempCelsius;

    @Column(name = "max_allowed_excursion_minutes", nullable = false)
    private Integer maxAllowedExcursionMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private SensorDevice assignedSensor;

    public static Shipment create(String trackingNumber, String originFacility, String destinationFacility, String productName,
                                  String batchNumber, BigDecimal minTempCelsius, BigDecimal maxTempCelsius, Integer maxAllowedExcursionMinutes) {
        return Shipment.builder()
                .trackingNumber(trackingNumber)
                .originFacility(originFacility)
                .destinationFacility(destinationFacility)
                .productName(productName)
                .batchNumber(batchNumber)
                .minTempCelsius(minTempCelsius)
                .maxTempCelsius(maxTempCelsius)
                .maxAllowedExcursionMinutes(maxAllowedExcursionMinutes)
                .status(ShipmentStatus.CREATED)
                .build();
    }

    public void startShipment(SensorDevice sensorDevice) {
        if (this.status != ShipmentStatus.CREATED) {
            throw new InvalidShipmentStateTransitionException(ShipmentStatus.IN_TRANSIT, this.getStatus());
        }
        this.assignedSensor = sensorDevice;
        this.status = ShipmentStatus.IN_TRANSIT;
    }

    public void deliver() {
        if (this.status != ShipmentStatus.IN_TRANSIT) {
            throw new InvalidShipmentStateTransitionException(ShipmentStatus.DELIVERED, this.getStatus());
        }
        this.status = ShipmentStatus.DELIVERED;
    }

    public void cancel() {
        if (this.status != ShipmentStatus.CREATED) {
            throw new InvalidShipmentStateTransitionException(ShipmentStatus.CANCELLED, this.getStatus());
        }
        this.status = ShipmentStatus.CANCELLED;
    }

    public void compromise() {
        if (ShipmentStatus.IN_TRANSIT != this.getStatus()) {
            throw new InvalidShipmentStateTransitionException(ShipmentStatus.COMPROMISED, this.getStatus());
        }
        this.status = ShipmentStatus.COMPROMISED;
    }


    public TelemetryRecord recordTelemetry(SensorDevice reportingSensor,
                                           BigDecimal temperatureCelsius, Double humidityPercentage,
                                           Instant timestamp, Double latitude, Double longitude) {
        this.verifyCanReceiveTelemetry();
        this.verifyAssignedSensor(reportingSensor);
        reportingSensor.verifyIsAssigned();
        return TelemetryRecord.createRecord(this, reportingSensor, temperatureCelsius, humidityPercentage,
                timestamp, latitude, longitude);
    }

    public void verifyCanReceiveTelemetry() {
        if (ShipmentStatus.IN_TRANSIT != this.getStatus()) {
            throw new InvalidShipmentStateException(this.trackingNumber, this.status);
        }
    }

    public void verifyAssignedSensor(SensorDevice reportingSensor) {
        if (this.assignedSensor == null || !this.assignedSensor.getId().equals(reportingSensor.getId())) {
            throw new InconsistentTelemetrySensorException(
                    reportingSensor.getSerialNumber(),
                    this.assignedSensor != null ? this.assignedSensor.getSerialNumber() : "NONE"
            );
        }
    }

    public ThermalExcursion reportExcursion(BigDecimal recordedTemperature, Instant timestamp) {
        ThermalExcursion thermalExcursion = ThermalExcursion.createRecord(recordedTemperature, timestamp, this);
        if (thermalExcursion.isCritical()) {
            this.compromise();
        }
        return thermalExcursion;
    }
}
