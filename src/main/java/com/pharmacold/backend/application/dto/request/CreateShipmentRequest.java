package com.pharmacold.backend.application.dto.request;

import com.pharmacold.backend.application.dto.validator.ValidTemperatureRange;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@ValidTemperatureRange
public record CreateShipmentRequest(
        @NotBlank
        @Pattern(regexp = "^PHARMA-\\d{4}-[A-Z0-9]{4}$", message = "Tracking must be like e.g. PHARMA-2026-X89B")
        String trackingNumber,

        @NotBlank
        @Size(min = 3, max = 100)
        String originFacility,

        @NotBlank
        @Size(min = 3, max = 100)
        String destinationFacility,

        @NotBlank
        String productName,

        @NotBlank
        String batchNumber,

        @NotNull
        @Digits(integer = 3, fraction = 2, message = "Minimum temperature must have at most 3 integer and 2 decimal places")
        BigDecimal minTempCelsius,

        @NotNull
        @Digits(integer = 3, fraction = 2, message = "Maximum temperature must have at most 3 integer and 2 decimal places")
        BigDecimal maxTempCelsius,

        @NotNull
        Integer maxAllowedExcursionMinutes
) {
}
