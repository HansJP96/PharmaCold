package com.pharmacold.backend.application.mapper;

import com.pharmacold.backend.application.dto.response.SensorDeviceResponse;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.application.mapper.config.ApplicationMapperConfig;
import org.mapstruct.Mapper;

@Mapper(
        config = ApplicationMapperConfig.class
)
public interface SensorDeviceMapper {

    SensorDeviceResponse toResponse(SensorDevice sensorDevice);
}
