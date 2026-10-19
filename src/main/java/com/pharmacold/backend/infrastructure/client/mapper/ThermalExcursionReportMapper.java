package com.pharmacold.backend.infrastructure.client.mapper;

import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import com.pharmacold.backend.infrastructure.client.dto.ThermalExcursionReportPayload;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ThermalExcursionReportMapper {

    ThermalExcursionReportPayload toPayload(ThermalExcursionDetectedEvent excursionDetectedEvent);
}
