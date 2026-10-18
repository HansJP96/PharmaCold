package com.pharmacold.backend.domain.event;

import com.pharmacold.backend.domain.enums.ExcursionSeverity;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record ThermalExcursionDetectedEvent(
        Instant detectedAt,
        BigDecimal recordedTemperature,
        ExcursionSeverity severity,
        String trackingNumber,
        BigDecimal minTempCelsius,
        BigDecimal maxTempCelsius
) implements TrackableNotification {

    @Override
    public String getContextSummary() {
        return "Shipment: %s | Severity: %s".formatted(this.trackingNumber(), this.severity());
    }
}
