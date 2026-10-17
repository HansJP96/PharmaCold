package com.pharmacold.backend.application.mapper;


import com.pharmacold.backend.application.dto.response.TelemetryRecordResponse;
import com.pharmacold.backend.domain.model.TelemetryRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TelemetryRecordMapper {

    TelemetryRecordResponse toResponse(TelemetryRecord telemetryRecord);
}
