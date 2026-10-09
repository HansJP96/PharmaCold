package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.ShipmentStatus;
import lombok.Getter;

@Getter
public class InvalidShipmentStateTransitionException extends DomainException {

    private final ShipmentStatus requiredState;
    private final ShipmentStatus actualState;

    public InvalidShipmentStateTransitionException(ShipmentStatus requiredState, ShipmentStatus actualState){
        super("The selected status [%s] is not allowed to set from [%s]".formatted(requiredState, actualState));
        this.requiredState = requiredState;
        this.actualState = actualState;
    }
}
