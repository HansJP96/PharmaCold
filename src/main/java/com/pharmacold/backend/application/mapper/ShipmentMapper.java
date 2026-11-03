package com.pharmacold.backend.application.mapper;

import com.pharmacold.backend.application.dto.response.ShipmentResponse;
import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.application.mapper.config.ApplicationMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        config = ApplicationMapperConfig.class
)
public interface ShipmentMapper {

    @Mapping(source = "assignedSensor.id", target = "assignedSensorId")
    ShipmentResponse toResponse(Shipment shipment);
}
