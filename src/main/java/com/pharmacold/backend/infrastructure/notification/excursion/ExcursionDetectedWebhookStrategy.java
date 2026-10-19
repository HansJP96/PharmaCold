package com.pharmacold.backend.infrastructure.notification.excursion;

import com.pharmacold.backend.application.notification.NotificationStrategy;
import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import com.pharmacold.backend.infrastructure.client.FleetLogisticsClient;
import com.pharmacold.backend.infrastructure.client.mapper.ThermalExcursionReportMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Component
public class ExcursionDetectedWebhookStrategy implements NotificationStrategy<ThermalExcursionDetectedEvent> {

    private final FleetLogisticsClient fleetLogisticsClient;
    private final ThermalExcursionReportMapper thermalExcursionReportMapper;

    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    ConnectException.class,
                    SocketTimeoutException.class,
                    HttpClientErrorException.TooManyRequests.class}
            ,
            noRetryFor = {HttpClientErrorException.class},
            maxAttemptsExpression = "${retry.notifications.maxAttempts}",
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
        log.info("- [WEBHOOK NOTIFICATION] Reporting incident regarding the shipment: {} -", event.trackingNumber());
        fleetLogisticsClient.reportExcursionIncident(thermalExcursionReportMapper.toPayload(event));
    }
}
