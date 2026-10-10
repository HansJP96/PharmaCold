package com.pharmacold.backend.application.dto.response;

import com.pharmacold.backend.domain.enums.ShipmentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record ShipmentResponse(
        Long id,
        String trackingNumber,
        String originFacility,
        String destinationFacility,
        String productName,
        String batchNumber,
        BigDecimal minTempCelsius,
        BigDecimal maxTempCelsius,
        Integer maxAllowedExcursionMinutes,
        ShipmentStatus status,
        Long assignedSensor,
        Instant createdAt
) {
}
