package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.SensorStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sensor_devices")
public class SensorDevice extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(nullable = false)
    private String model;

    @Max(value = 100, message = "Battery percentage cannot exceed 100")
    @Min(value = 0, message = "Battery percentage cannot be negative")
    @Column(name = "battery_percentage", nullable = false)
    private Integer batteryPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorStatus status;

    @Column(name = "last_calibrated_at")
    private Instant lastCalibratedAt;
}
