package com.pharmacold.backend.domain.model;

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
@Table(name = "telemetry_records",
        indexes = {
                @Index(name = "idx_telemetry_timestamp", columnList = "timestamp, timestamp DESC"),
        }
)
public class TelemetryRecord extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Digits(integer = 3, fraction = 2, message = "Temperature must have at most 3 integer and 2 decimal places")
    @Column(name = "temperature_celsius", nullable = false)
    private BigDecimal temperatureCelsius;

    @Column(name = "humidity_percentage", nullable = false)
    private Double humidityPercentage;

    private Double latitude;

    private Double longitude;

    @Column(name = "is_excursion", nullable = false)
    private Boolean isExcursion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment relatedShipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private SensorDevice sensorDispatcher;
}
