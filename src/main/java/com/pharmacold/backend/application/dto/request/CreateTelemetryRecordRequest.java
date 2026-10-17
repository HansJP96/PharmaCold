package com.pharmacold.backend.application.dto.request;


import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record CreateTelemetryRecordRequest(

        @NotBlank
        String shipmentTrackingNumber,

        @NotBlank
        String sensorSerialNumber,

        @NotNull
        @Digits(integer = 3, fraction = 2, message = "Temperature must have at most 3 integer and 2 decimal places")
        BigDecimal temperatureCelsius,

        @Max(value = 100, message = "Humidity percentage cannot exceed 100")
        @Min(value = 0, message = "Humidity percentage cannot be negative")
        @NotNull
        Double humidityPercentage,

        @NotNull
        @PastOrPresent
        Instant timestamp,

        Double latitude,
        Double longitude
        ) {
}
