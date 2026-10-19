package com.pharmacold.backend.infrastructure.client;

import com.pharmacold.backend.infrastructure.client.dto.ThermalExcursionReportPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.PostExchange;

public interface FleetLogisticsClient {

    @PostExchange("/incident")
    ResponseEntity<Void> reportExcursionIncident(ThermalExcursionReportPayload excursionReportPayload);
}
