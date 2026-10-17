package com.pharmacold.backend.domain.exception;

import lombok.Getter;

@Getter
public class InconsistentTelemetrySensorException extends DomainException {

    private final String telemetrySensor;
    private final String shipmentAssignedSensor;

    public InconsistentTelemetrySensorException(String telemetrySensor, String shipmentAssignedSensor) {
        super("The telemetry record associated sensor [%s] is not the same as the shipment assigned sensor [%s]"
                .formatted(telemetrySensor, shipmentAssignedSensor)
        );
        this.telemetrySensor = telemetrySensor;
        this.shipmentAssignedSensor = shipmentAssignedSensor;
    }
}
