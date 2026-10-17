package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.ShipmentStatus;
import lombok.Getter;

@Getter
public class InvalidShipmentStateException extends DomainException {

    private final String trackingNumber;
    private final ShipmentStatus currentState;

    public InvalidShipmentStateException(String trackingNumber, ShipmentStatus currentState) {
        super("Shipment [%s] cannot perform operation because its status is [%s]".formatted(trackingNumber, currentState));
        this.trackingNumber = trackingNumber;
        this.currentState = currentState;
    }
}
