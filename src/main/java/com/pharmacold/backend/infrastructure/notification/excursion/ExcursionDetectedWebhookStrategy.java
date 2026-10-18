package com.pharmacold.backend.infrastructure.notification.excursion;

import com.pharmacold.backend.application.notification.NotificationStrategy;
import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
public class ExcursionDetectedWebhookStrategy implements NotificationStrategy<ThermalExcursionDetectedEvent> {

    private final RestClient restClient = RestClient.create();

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
        String targetEndpoint = "https://fleet.com/api/...";

        ResponseEntity<Void> response =
                restClient
                        .post()
                        .uri(targetEndpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(event)
                        .retrieve()
                        .toBodilessEntity();
        log.info("[WEBHOOK ALERT] POST  {} - PAYLOAD: {}", targetEndpoint, event);
    }
}
