package com.pharmacold.backend.application.mapper;

import com.pharmacold.backend.application.dto.request.CreateSensorDeviceRequest;
import com.pharmacold.backend.application.dto.response.SensorDeviceResponse;
import com.pharmacold.backend.domain.model.SensorDevice;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SensorDeviceMapper {

    SensorDevice toEntity(CreateSensorDeviceRequest request);

    SensorDeviceResponse toResponse(SensorDevice sensorDevice);
}
