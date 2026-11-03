package com.pharmacold.backend.application.mapper;


import com.pharmacold.backend.application.dto.response.TelemetryRecordResponse;
import com.pharmacold.backend.domain.model.TelemetryRecord;
import com.pharmacold.backend.application.mapper.config.ApplicationMapperConfig;
import org.mapstruct.Mapper;

@Mapper(
        config = ApplicationMapperConfig.class
)
public interface TelemetryRecordMapper {

    TelemetryRecordResponse toResponse(TelemetryRecord telemetryRecord);
}
