package com.pharmacold.backend.application.service;

import com.pharmacold.backend.application.dto.request.CreateShipmentRequest;
import com.pharmacold.backend.application.dto.response.ShipmentResponse;
import com.pharmacold.backend.application.mapper.ShipmentMapper;
import com.pharmacold.backend.domain.enums.ShipmentStatus;
import com.pharmacold.backend.domain.exception.DuplicateResourceException;
import com.pharmacold.backend.domain.exception.ResourceNotFoundException;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    private final SensorDeviceService sensorDeviceService;

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        if (shipmentRepository.existsByTrackingNumber(request.trackingNumber())) {
            throw new DuplicateResourceException(Shipment.class.getSimpleName(), "trackingNumber", request.trackingNumber());
        }
        Shipment shipment = Shipment.create(
                request.trackingNumber(),
                request.originFacility(),
                request.destinationFacility(),
                request.productName(),
                request.batchNumber(),
                request.minTempCelsius(),
                request.maxTempCelsius(),
                request.maxAllowedExcursionMinutes()
        );
        Shipment savedShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toResponse(savedShipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        Shipment shipment = findShipmentById(id);
        return shipmentMapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = findShipmentByTrackingNumber(trackingNumber);
        return shipmentMapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public Slice<ShipmentResponse> getShipments(ShipmentStatus status, Pageable pageable) {
        return shipmentRepository.findByStatus(status, pageable)
                .map(shipmentMapper::toResponse);
    }

    @Transactional
    public ShipmentResponse startTransit(Long shipmentId, String sensorSerialNumber) {
        Shipment shipment = findShipmentById(shipmentId);
        SensorDevice sensorToAssign = sensorDeviceService.findSensorDeviceBySerialNumber(sensorSerialNumber);
        shipment.startShipment(sensorToAssign);
        sensorDeviceService.assignSensorToShipment(sensorToAssign);
        Shipment inTransitShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toResponse(inTransitShipment);
    }

    @Transactional
    public ShipmentResponse deliverShipment(String trackingNumber) {
        Shipment shipment = findShipmentByTrackingNumber(trackingNumber);
        shipment.deliver();
        sensorDeviceService.markSensorForCalibration(shipment.getAssignedSensor());
        Shipment deliveredShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toResponse(deliveredShipment);
    }

    @Transactional
    public ShipmentResponse cancelShipment(String trackingNumber) {
        Shipment shipment = findShipmentByTrackingNumber(trackingNumber);
        shipment.cancel();
        Shipment cancelledShipment = shipmentRepository.save(shipment);
        return shipmentMapper.toResponse(cancelledShipment);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected Shipment findShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Shipment.class.getSimpleName(), "id", id));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected Shipment findShipmentByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException(Shipment.class.getSimpleName(), "trackingNumber", trackingNumber));
    }
}
