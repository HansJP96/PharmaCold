package com.pharmacold.backend.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record TelemetryRecordResponse(
        Instant timestamp,
        BigDecimal temperatureCelsius,
        boolean isExcursion
) {
}
