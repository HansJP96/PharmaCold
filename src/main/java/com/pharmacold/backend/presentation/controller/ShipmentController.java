package com.pharmacold.backend.presentation.controller;

import com.pharmacold.backend.application.dto.request.CreateShipmentRequest;
import com.pharmacold.backend.application.dto.response.ShipmentResponse;
import com.pharmacold.backend.application.service.ShipmentService;
import com.pharmacold.backend.domain.enums.ShipmentStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RequiredArgsConstructor
@RequestMapping("/shipments")
@RestController
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR','QUALITY_AUDITOR')")
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
        ShipmentResponse response = shipmentService.getShipmentById(id);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR','CARRIER','QUALITY_AUDITOR')")
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        ShipmentResponse response = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR','QUALITY_AUDITOR')")
    @GetMapping
    public ResponseEntity<Slice<ShipmentResponse>> getShipments(
            @RequestParam(required = false) ShipmentStatus status,
            Pageable pageable
    ) {
        Slice<ShipmentResponse> response = shipmentService.getShipments(status, pageable);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR')")
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse response = shipmentService.createShipment(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR')")
    @PostMapping("/{id:[0-9]+}/sensor/{serialNumber}/start-transit")
    public ResponseEntity<ShipmentResponse> startTransit(@PathVariable Long id, @PathVariable String serialNumber) {
        ShipmentResponse response = shipmentService.startTransit(id, serialNumber);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','CARRIER')")
    @PostMapping("/tracking/{trackingNumber}/deliver")
    public ResponseEntity<ShipmentResponse> deliverShipment(@PathVariable String trackingNumber) {
        ShipmentResponse response = shipmentService.deliverShipment(trackingNumber);
        return ResponseEntity.ok().body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_OPERATOR')")
    @PostMapping("/tracking/{trackingNumber}/cancel")
    public ResponseEntity<ShipmentResponse> cancelShipment(@PathVariable String trackingNumber) {
        ShipmentResponse response = shipmentService.cancelShipment(trackingNumber);
        return ResponseEntity.ok().body(response);
    }
}
