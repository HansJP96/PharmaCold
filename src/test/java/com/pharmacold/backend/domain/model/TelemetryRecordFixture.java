package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.application.dto.request.CreateTelemetryRecordRequest;

import java.math.BigDecimal;
import java.time.Instant;

public class TelemetryRecordFixture {

    public static CreateTelemetryRecordRequest getCreateTelemetryRequest(BigDecimal temp) {
        return CreateTelemetryRecordRequest.builder()
                .sensorSerialNumber("IOT-SNS-00001")
                .shipmentTrackingNumber("PHARMA-2026-X89B")
                .temperatureCelsius(temp)
                .timestamp(Instant.now())
                .humidityPercentage(77.0)
                .latitude(44.787)
                .longitude(71.937)
                .build();
    }
}
