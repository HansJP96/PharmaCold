package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.SensorStatus;
import lombok.Getter;

@Getter
public class InvalidSensorStateException extends DomainException {

    private final SensorStatus currentState;

    public InvalidSensorStateException(SensorStatus currentState){
        super("The selected sensor state [%s] is not allowed to process".formatted(currentState));
        this.currentState = currentState;
    }
}
