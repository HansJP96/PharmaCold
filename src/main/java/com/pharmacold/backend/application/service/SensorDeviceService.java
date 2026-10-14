package com.pharmacold.backend.application.service;

import com.pharmacold.backend.application.dto.request.CreateSensorDeviceRequest;
import com.pharmacold.backend.application.dto.response.SensorDeviceResponse;
import com.pharmacold.backend.application.mapper.SensorDeviceMapper;
import com.pharmacold.backend.domain.enums.SensorStatus;
import com.pharmacold.backend.domain.exception.DuplicateResourceException;
import com.pharmacold.backend.domain.exception.ResourceNotFoundException;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.repository.SensorDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SensorDeviceService {

    private final SensorDeviceRepository sensorDeviceRepository;
    private final SensorDeviceMapper sensorDeviceMapper;

    @Transactional
    public SensorDeviceResponse registerSensorDevice(CreateSensorDeviceRequest request) {
        if (sensorDeviceRepository.existsBySerialNumber(request.serialNumber())) {
            throw new DuplicateResourceException(SensorDevice.class.getSimpleName(), "serialNumber", request.serialNumber());
        }
        SensorDevice sensor = SensorDevice.create(
                request.serialNumber(),
                request.model(),
                request.batteryPercentage()
        );
        SensorDevice savedSensor = sensorDeviceRepository.save(sensor);
        return sensorDeviceMapper.toResponse(savedSensor);
    }

    @Transactional(readOnly = true)
    public SensorDeviceResponse getSensorDeviceById(Long id) {
        SensorDevice sensorDevice = findSensorDeviceById(id);
        return sensorDeviceMapper.toResponse(sensorDevice);
    }

    @Transactional(readOnly = true)
    public SensorDeviceResponse getSensorDeviceBySerialNumber(String serialNumber) {
        SensorDevice sensor = findSensorDeviceBySerialNumber(serialNumber);
        return sensorDeviceMapper.toResponse(sensor);
    }

    @Transactional(readOnly = true)
    public Slice<SensorDeviceResponse> findAllAvailableSensorDevices(Pageable pageable) {
        return sensorDeviceRepository.findByStatus(SensorStatus.AVAILABLE, pageable)
                .map(sensorDeviceMapper::toResponse);
    }

    @Transactional
    public SensorDeviceResponse calibrateSensorDevice(Long id) {
        SensorDevice sensorDevice = findSensorDeviceById(id);
        sensorDevice.calibrate();
        SensorDevice calibratedSensorDevice = sensorDeviceRepository.save(sensorDevice);
        return sensorDeviceMapper.toResponse(calibratedSensorDevice);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected void assignSensorToShipment(SensorDevice sensorDevice){
        sensorDevice.assignToShipment();
        sensorDeviceRepository.save(sensorDevice);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected void markSensorForCalibration(SensorDevice sensor){
        sensor.markForCalibration();
        sensorDeviceRepository.save(sensor);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected SensorDevice findSensorDeviceById(Long id) {
        return sensorDeviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SensorDevice.class.getSimpleName(), "id", id));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected SensorDevice findSensorDeviceBySerialNumber(String serialNumber) {
        return sensorDeviceRepository.findBySerialNumber(serialNumber).
                orElseThrow(() -> new ResourceNotFoundException(SensorDevice.class.getSimpleName(), "serialNumber", serialNumber));
    }

}
