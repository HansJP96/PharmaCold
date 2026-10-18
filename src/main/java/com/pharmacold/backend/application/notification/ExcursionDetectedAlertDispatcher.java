package com.pharmacold.backend.application.notification;

import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ExcursionDetectedAlertDispatcher {

    private final List<NotificationStrategy<ThermalExcursionDetectedEvent>> alertStrategies;

    public void dispatch(ThermalExcursionDetectedEvent excursionDetectedEvent) {
        for (NotificationStrategy<ThermalExcursionDetectedEvent> alert : alertStrategies) {
            alert.send(excursionDetectedEvent);
        }
    }
}
