package com.pharmacold.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "temperature_celsius", nullable = false)
    private Double temperatureCelsius;

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
