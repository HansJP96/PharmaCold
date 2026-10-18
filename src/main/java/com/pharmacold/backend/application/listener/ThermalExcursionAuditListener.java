package com.pharmacold.backend.application.listener;

import com.pharmacold.backend.application.notification.ExcursionDetectedAlertDispatcher;
import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class ThermalExcursionAuditListener {

    private final ExcursionDetectedAlertDispatcher alertDispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onThermalExcursionDetected(ThermalExcursionDetectedEvent thermalExcursionEvent) {
        log.warn("Thermal excursion detected for shipment [{}]! Recorded: {}°C (Allowed: {}°C to {}°C) | Severity: {} | Time: {}",
                thermalExcursionEvent.trackingNumber(),
                thermalExcursionEvent.recordedTemperature(),
                thermalExcursionEvent.minTempCelsius(),
                thermalExcursionEvent.maxTempCelsius(),
                thermalExcursionEvent.severity(),
                thermalExcursionEvent.detectedAt()
        );
        alertDispatcher.dispatch(thermalExcursionEvent);
    }
}
