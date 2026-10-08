package com.pharmacold.backend.application.dto.response;

import com.pharmacold.backend.domain.enums.SensorStatus;
import java.time.Instant;

public record SensorDeviceResponse(
        Long id,
        String serialNumber,
        String model,
        Integer batteryPercentage,
        SensorStatus status,
        Instant lastCalibratedAt,
        Instant createdAt
) {
}
