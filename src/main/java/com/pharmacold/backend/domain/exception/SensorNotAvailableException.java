package com.pharmacold.backend.domain.exception;

import com.pharmacold.backend.domain.enums.SensorStatus;
import lombok.Getter;

@Getter
public class SensorNotAvailableException extends DomainException {

    private final String serialNumber;
    private final SensorStatus actualState;

    public SensorNotAvailableException(String serialNumber, SensorStatus actualState) {
        super("Sensor with serial number [%s] is already in state [%s]".formatted(serialNumber, actualState));
        this.serialNumber = serialNumber;
        this.actualState = actualState;
    }
}
