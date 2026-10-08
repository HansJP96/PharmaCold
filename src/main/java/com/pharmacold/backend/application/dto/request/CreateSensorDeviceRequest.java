package com.pharmacold.backend.application.dto.request;

import jakarta.validation.constraints.*;

public record CreateSensorDeviceRequest(
        @NotBlank
        @Pattern(regexp = "^IOT-SNS-\\d{5}$", message = "The serial must have the format IOT-SNS-XXXXX")
        String serialNumber,

        @NotBlank
        String model,

        @Max(value = 100, message = "Battery percentage cannot exceed 100")
        @Min(value = 0, message = "Battery percentage cannot be negative")
        @NotNull
        Integer batteryPercentage
) {
}
