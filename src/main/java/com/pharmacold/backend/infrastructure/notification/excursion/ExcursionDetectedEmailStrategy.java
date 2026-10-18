package com.pharmacold.backend.infrastructure.notification.excursion;

import com.pharmacold.backend.application.notification.NotificationStrategy;
import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
public class ExcursionDetectedEmailStrategy implements NotificationStrategy<ThermalExcursionDetectedEvent> {

    @Retryable(
            retryFor = {ResourceAccessException.class, ConnectException.class, SocketTimeoutException.class},
            noRetryFor = {HttpClientErrorException.class},
            backoff = @Backoff(
                    delayExpression = "${retry.notifications.delay}",
                    multiplierExpression = "${retry.notifications.multiplier}",
                    maxDelayExpression = "${retry.notifications.maxDelay}",
                    random = true
            )
    )
    @Async("notificationTaskExecutor")
    @Override
    public void send(ThermalExcursionDetectedEvent event) {
        String sender = "backend@gmail.com";
        String recipient = "people@gmail.com";
        String subject = "Thermal Excursion Detected from shipment %s";
        String body = "Automated Alert \n " +
                "An excursion has been detected from shipment %s with severity %s at temperture %s°C at datetime %s";

        log.info("From: {}", sender);
        log.info("To: {}", recipient);
        log.info("Subject: {}", subject.formatted(event.trackingNumber()));
        log.info(
                "{}",
                body.formatted(event.trackingNumber(), event.severity(), event.recordedTemperature(), event.detectedAt())
        );
    }


}
