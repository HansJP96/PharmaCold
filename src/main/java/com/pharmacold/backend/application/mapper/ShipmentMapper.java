package com.pharmacold.backend.application.mapper;

import com.pharmacold.backend.application.dto.request.CreateShipmentRequest;
import com.pharmacold.backend.application.dto.response.ShipmentResponse;
import com.pharmacold.backend.domain.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ShipmentMapper {

    Shipment toEntity(CreateShipmentRequest request);

    @Mapping(source = "assignedSensor.id", target = "assignedSensorId")
    ShipmentResponse toResponse(Shipment shipment);
}
