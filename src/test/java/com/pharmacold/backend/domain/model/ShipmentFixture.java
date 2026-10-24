package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.application.dto.request.CreateShipmentRequest;
import com.pharmacold.backend.domain.enums.ShipmentStatus;
import org.junit.jupiter.params.provider.Arguments;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.pharmacold.backend.domain.model.SensorDeviceFixture.getMismatchedSensor;

public class ShipmentFixture {

    private static CreateShipmentRequest.CreateShipmentRequestBuilder validShipmentRequestBuilder() {
        return CreateShipmentRequest.builder()
                .trackingNumber("PHARMA-2026-X89B")
                .productName("Gyossillycus")
                .originFacility("Pharma East")
                .destinationFacility("Pharma West")
                .batchNumber("15D2026")
                .minTempCelsius(new BigDecimal("2.00"))
                .maxTempCelsius(new BigDecimal("22.00"))
                .maxAllowedExcursionMinutes(2);
    }

    private static Shipment.ShipmentBuilder validShipmentBuilder() {
        CreateShipmentRequest validRequest = getValidShipmentRequest();
        return Shipment.builder()
                .id(1L)
                .trackingNumber(validRequest.trackingNumber())
                .productName(validRequest.productName())
                .originFacility(validRequest.originFacility())
                .destinationFacility(validRequest.destinationFacility())
                .batchNumber(validRequest.batchNumber())
                .status(ShipmentStatus.CREATED)
                .minTempCelsius(validRequest.minTempCelsius())
                .maxTempCelsius(validRequest.maxTempCelsius())
                .maxAllowedExcursionMinutes(validRequest.maxAllowedExcursionMinutes());
    }

    public static CreateShipmentRequest getValidShipmentRequest() {
        return validShipmentRequestBuilder().build();
    }

    public static CreateShipmentRequest getInvalidShipmentRequest() {
        return validShipmentRequestBuilder()
                .minTempCelsius(new BigDecimal("-2.00"))
                .maxTempCelsius(new BigDecimal("-2.00"))
                .build();
    }


    public static Shipment getCreatedShipment() {
        return validShipmentBuilder()
                .build();
    }

    public static Shipment getStartedShipment() {
        return validShipmentBuilder()
                .assignedSensor(SensorDeviceFixture.getAvailableSensor())
                .build();
    }

    public static Shipment getInTransitShipment() {
        return validShipmentBuilder()
                .status(ShipmentStatus.IN_TRANSIT)
                .assignedSensor(SensorDeviceFixture.getAssignedSensor())
                .build();
    }

    public static Shipment getInTransitShipmentWithSensor(SensorDevice sensor) {
        return validShipmentBuilder()
                .status(ShipmentStatus.IN_TRANSIT)
                .assignedSensor(sensor)
                .build();
    }

    public static Shipment getInTransitShipmentWithTempLimits(BigDecimal minTempCelsius, BigDecimal maxTempCelsius) {
        return validShipmentBuilder()
                .status(ShipmentStatus.IN_TRANSIT)
                .assignedSensor(SensorDeviceFixture.getAssignedSensor())
                .minTempCelsius(minTempCelsius)
                .maxTempCelsius(maxTempCelsius)
                .build();
    }

    public static Shipment getShipmentWithStatus(ShipmentStatus status) {
        return validShipmentBuilder()
                .status(status)
                .build();
    }

    public static Stream<Arguments> getInconsistentShipmentSensor() {
        SensorDevice mismatchedSensor = getMismatchedSensor();
        return Stream.of(
                Arguments.of(
                        getInTransitShipmentWithSensor(null),
                        "NONE",
                        "NULL"
                ),
                Arguments.of(
                        getInTransitShipmentWithSensor(mismatchedSensor),
                        mismatchedSensor.getSerialNumber(),
                        "NOT MATCH Id"
                )
        );
    }

    public static Shipment getShipmentToSave() {
        return validShipmentBuilder()
                .id(null)
                .build();
    }
}
