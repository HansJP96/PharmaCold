package com.pharmacold.backend.domain.exception;

import lombok.Getter;

@Getter
public class InvalidTemperatureRangeException extends DomainException {

    private final String trackingNumber;
    private final String minTemperature;
    private final String maxTemperature;

    public InvalidTemperatureRangeException(String trackingNumber, String minTemperature, String maxTemperature) {
        super("Shipment [%s] with temperature ranges min: [%s°C] - max [%s°C] is not valid"
                .formatted(trackingNumber, minTemperature, maxTemperature)
        );
        this.trackingNumber = trackingNumber;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }
}
