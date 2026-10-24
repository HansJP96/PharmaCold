package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.application.dto.request.CreateSensorDeviceRequest;
import com.pharmacold.backend.domain.enums.SensorStatus;

public class SensorDeviceFixture {

    private static CreateSensorDeviceRequest.CreateSensorDeviceRequestBuilder validSensorRequestBuilder() {
        return CreateSensorDeviceRequest.builder()
                .serialNumber("IOT-SNS-00001")
                .model("Samsung XPA")
                .batteryPercentage(100);
    }

    private static SensorDevice.SensorDeviceBuilder validSensorDeviceBuilder() {
        CreateSensorDeviceRequest request = getValidSensorRequest();
        return SensorDevice.builder()
                .id(1L)
                .serialNumber(request.serialNumber())
                .model(request.model())
                .batteryPercentage(request.batteryPercentage())
                .status(SensorStatus.AVAILABLE)
                .lastCalibratedAt(null);
    }

    public static CreateSensorDeviceRequest getValidSensorRequest() {
        return validSensorRequestBuilder().build();
    }

    public static SensorDevice getAvailableSensor() {
        return validSensorDeviceBuilder().build();
    }

    public static SensorDevice getAssignedSensor() {
        return validSensorDeviceBuilder()
                .status(SensorStatus.ASSIGNED)
                .build();
    }


    public static SensorDevice getSensorWithStatus(SensorStatus status) {
        return validSensorDeviceBuilder()
                .status(status)
                .build();
    }

    public static SensorDevice getMismatchedSensor() {
        return validSensorDeviceBuilder()
                .id(12345L)
                .serialNumber("IOT-XXX-00000")
                .build();
    }

    public static SensorDevice getSensorToSave() {
        return validSensorDeviceBuilder()
                .id(null)
                .build();
    }
}
