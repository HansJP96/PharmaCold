package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.ExcursionSeverity;
import com.pharmacold.backend.domain.enums.ExcursionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class ThermalExcursionFixture {

    public static ThermalExcursion getValidThermalExcursion() {
        return ThermalExcursion.builder()
                .id(1L)
                .detectedAt(Instant.now())
                .recordedTemperature(BigDecimal.valueOf(10))
                .severity(ExcursionSeverity.WARNING)
                .status(ExcursionStatus.OPEN)
                .compromisedShipment(ShipmentFixture.getInTransitShipment())
                .build();
    }

}
