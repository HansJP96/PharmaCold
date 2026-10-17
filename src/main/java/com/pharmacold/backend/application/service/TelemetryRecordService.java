package com.pharmacold.backend.application.service;

import com.pharmacold.backend.application.dto.request.CreateTelemetryRecordRequest;
import com.pharmacold.backend.application.dto.response.TelemetryRecordResponse;
import com.pharmacold.backend.application.mapper.TelemetryRecordMapper;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.domain.model.TelemetryRecord;
import com.pharmacold.backend.domain.repository.TelemetryRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TelemetryRecordService {

    private final TelemetryRecordRepository telemetryRecordRepository;
    private final TelemetryRecordMapper telemetryRecordMapper;

    private final SensorDeviceService sensorDeviceService;
    private final ShipmentService shipmentService;
    private final ThermalExcursionService thermalExcursionService;

    @Transactional
    public TelemetryRecordResponse createTelemetryRecord(CreateTelemetryRecordRequest request) {
        Shipment associatedShipment = shipmentService.findShipmentByTrackingNumber(request.shipmentTrackingNumber());
        SensorDevice reportingSensor = sensorDeviceService.findSensorDeviceBySerialNumber(request.sensorSerialNumber());

        TelemetryRecord telemetryRecord = associatedShipment.recordTelemetry(
                reportingSensor, request.temperatureCelsius(), request.humidityPercentage(), request.timestamp(),
                request.latitude(), request.longitude()
        );
        if (telemetryRecord.getIsExcursion()) {
            thermalExcursionService.generateThermalExcursionRecord(
                    telemetryRecord.getTemperatureCelsius(),
                    telemetryRecord.getTimestamp(),
                    associatedShipment
            );
        }

        TelemetryRecord savedTelemetryRecord = telemetryRecordRepository.save(telemetryRecord);
        return telemetryRecordMapper.toResponse(savedTelemetryRecord);
    }
}
