package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.ShipmentStatus;
import lombok.Getter;

@Getter
public class InvalidShipmentStateTransitionException extends DomainException {

    private final ShipmentStatus targetState;
    private final ShipmentStatus currentState;

    public InvalidShipmentStateTransitionException(ShipmentStatus targetState, ShipmentStatus currentState){
        super("The selected shipment status [%s] is not allowed to set from [%s]".formatted(targetState, currentState));
        this.targetState = targetState;
        this.currentState = currentState;
    }
}
