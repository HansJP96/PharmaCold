package com.pharmacold.backend.infrastructure.client.dto;

import com.pharmacold.backend.domain.enums.ExcursionSeverity;

import java.math.BigDecimal;
import java.time.Instant;

public record ThermalExcursionReportPayload(
        Instant detectedAt,
        BigDecimal recordedTemperature,
        ExcursionSeverity severity,
        String trackingNumber,
        BigDecimal minTempCelsius,
        BigDecimal maxTempCelsius
) {
}
