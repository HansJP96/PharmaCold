package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.SensorStatus;
import com.pharmacold.backend.domain.exception.InvalidSensorStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.EnumSet;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(access = AccessLevel.PACKAGE)
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

    public static SensorDevice create(String serialNumber, String model, Integer batteryPercentage) {
        return SensorDevice.builder()
                .serialNumber(serialNumber)
                .model(model)
                .batteryPercentage(batteryPercentage)
                .status(SensorStatus.AVAILABLE)
                .build();
    }

    public void calibrate() {
        if (EnumSet.of(SensorStatus.ASSIGNED, SensorStatus.DECOMMISSIONED).contains(this.status)) {
            throw new InvalidSensorStateException(this.serialNumber, this.status);
        }
        this.status = SensorStatus.AVAILABLE;
        this.lastCalibratedAt = Instant.now();
    }

    public void verifyIsAssigned() {
        if (SensorStatus.ASSIGNED != this.status) {
            throw new InvalidSensorStateException(this.serialNumber, this.status);
        }
    }

    public void assignToShipment() {
        if (this.getStatus() != SensorStatus.AVAILABLE) {
            throw new InvalidSensorStateException(this.serialNumber, this.status);
        }
        this.status = SensorStatus.ASSIGNED;
    }

    public void markForCalibration() {
        this.status = SensorStatus.CALIBRATION_REQUIRED;
    }
}
