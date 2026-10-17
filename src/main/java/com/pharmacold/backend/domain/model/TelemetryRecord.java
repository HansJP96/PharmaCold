package com.pharmacold.backend.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.apache.commons.lang3.compare.ComparableUtils;

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

    @Max(value = 100, message = "Humidity percentage cannot exceed 100")
    @Min(value = 0, message = "Humidity percentage cannot be negative")
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

    public static TelemetryRecord createRecord(Shipment shipment, SensorDevice sensor,
                                               BigDecimal temperatureCelsius, Double humidityPercentage,
                                               Instant timestamp, Double latitude, Double longitude) {
        return TelemetryRecord.builder()
                .relatedShipment(shipment)
                .sensorDispatcher(sensor)
                .timestamp(timestamp)
                .temperatureCelsius(temperatureCelsius)
                .humidityPercentage(humidityPercentage)
                .isExcursion(
                        !ComparableUtils
                                .is(temperatureCelsius)
                                .between(shipment.getMinTempCelsius(), shipment.getMaxTempCelsius())
                )
                .latitude(latitude)
                .longitude(longitude)
                .build();

    }
}
