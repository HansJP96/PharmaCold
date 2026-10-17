package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.SensorStatus;
import lombok.Getter;

@Getter
@Deprecated
public class SensorNotAssignedException extends DomainException {

    private final String serialNumber;
    private final SensorStatus currentState;

    public SensorNotAssignedException(String serialNumber, SensorStatus currentState) {
        super("Sensor [%s] cannot perform operations because its status is [%s] (expected: ASSIGNED)".formatted(serialNumber, currentState));
        this.serialNumber = serialNumber;
        this.currentState = currentState;
    }
}
