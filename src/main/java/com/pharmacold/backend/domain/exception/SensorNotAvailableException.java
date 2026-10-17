package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.SensorStatus;
import lombok.Getter;

@Getter
@Deprecated
public class SensorNotAvailableException extends DomainException {

    private final String serialNumber;
    private final SensorStatus currentState;

    public SensorNotAvailableException(String serialNumber, SensorStatus currentState) {
        super("Sensor [%s] is already in state [%s]".formatted(serialNumber, currentState));
        this.serialNumber = serialNumber;
        this.currentState = currentState;
    }
}
