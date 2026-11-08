package com.pharmacold.backend.presentation.controller;

import com.pharmacold.backend.application.dto.request.CreateSensorDeviceRequest;
import com.pharmacold.backend.application.dto.response.SensorDeviceResponse;
import com.pharmacold.backend.application.service.SensorDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/sensors")
@RestController
public class SensorDeviceController {

    private final SensorDeviceService sensorDeviceService;

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR')")
    @GetMapping(path = "/{id:[0-9]+}")
    public ResponseEntity<SensorDeviceResponse> getSensorDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok().body(sensorDeviceService.getSensorDeviceById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR')")
    @GetMapping(path = "/serial/{serialNumber}")
    public ResponseEntity<SensorDeviceResponse> getSensorDeviceBySerialNumber(@PathVariable String serialNumber) {
        return ResponseEntity.ok().body(sensorDeviceService.getSensorDeviceBySerialNumber(serialNumber));
    }

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR')")
    @GetMapping(path = "/available")
    public ResponseEntity<Slice<SensorDeviceResponse>> getAvailableSensorDevices(Pageable pageable) {
        return ResponseEntity.ok().body(sensorDeviceService.findAllAvailableSensorDevices(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<SensorDeviceResponse> registerSensorDevice(@Valid @RequestBody CreateSensorDeviceRequest request) {
        SensorDeviceResponse response = sensorDeviceService.registerSensorDevice(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/{id:[0-9]+}/calibrate")
    public ResponseEntity<SensorDeviceResponse> calibrateSensorDevice(@PathVariable Long id) {
        return ResponseEntity.ok().body(sensorDeviceService.calibrateSensorDevice(id));
    }
}
