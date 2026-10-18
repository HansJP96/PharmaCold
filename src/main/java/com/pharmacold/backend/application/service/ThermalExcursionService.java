package com.pharmacold.backend.application.service;

import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.domain.model.ThermalExcursion;
import com.pharmacold.backend.domain.repository.ThermalExcursionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class ThermalExcursionService {

    private final ThermalExcursionRepository thermalExcursionRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ThermalExcursion generateThermalExcursionRecord(BigDecimal recordedTemperature, Instant timestamp, Shipment shipment) {
        ThermalExcursion thermalExcursion = shipment.reportExcursion(recordedTemperature, timestamp);
        ThermalExcursion savedThermalExcursion = thermalExcursionRepository.save(thermalExcursion);
        eventPublisher.publishEvent(savedThermalExcursion.toThermalExcursionDetectedEvent());
        return savedThermalExcursion;
    }
}
