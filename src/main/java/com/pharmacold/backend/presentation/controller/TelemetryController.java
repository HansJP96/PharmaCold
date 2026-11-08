package com.pharmacold.backend.presentation.controller;

import com.pharmacold.backend.application.dto.request.CreateTelemetryRecordRequest;
import com.pharmacold.backend.application.dto.response.TelemetryRecordResponse;
import com.pharmacold.backend.application.service.TelemetryRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/telemetry")
@RestController
public class TelemetryController {

    private final TelemetryRecordService telemetryRecordService;

    @PreAuthorize("hasAnyRole('ADMIN','CARRIER')")
    @PostMapping
    public ResponseEntity<TelemetryRecordResponse> createTelemetryRecord(@Valid @RequestBody CreateTelemetryRecordRequest request) {
        TelemetryRecordResponse response = telemetryRecordService.createTelemetryRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
