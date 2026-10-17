package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.SensorStatus;
import lombok.Getter;

@Getter
public class InvalidSensorStateException extends DomainException {

    private final String serialNumber;
    private final SensorStatus currentState;

    public InvalidSensorStateException(String serialNumber, SensorStatus currentState) {
        super("Sensor [%s] cannot perform operation because its status is [%s]".formatted(serialNumber, currentState));
        this.serialNumber = serialNumber;
        this.currentState = currentState;
    }
}
