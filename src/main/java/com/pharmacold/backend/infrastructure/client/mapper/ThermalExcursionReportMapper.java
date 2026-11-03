package com.pharmacold.backend.infrastructure.client.mapper;

import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import com.pharmacold.backend.infrastructure.client.dto.ThermalExcursionReportPayload;
import com.pharmacold.backend.infrastructure.client.mapper.config.ClientMapperConfig;
import org.mapstruct.Mapper;

@Mapper(
        config = ClientMapperConfig.class
)
public interface ThermalExcursionReportMapper {

    ThermalExcursionReportPayload toPayload(ThermalExcursionDetectedEvent excursionDetectedEvent);
}
