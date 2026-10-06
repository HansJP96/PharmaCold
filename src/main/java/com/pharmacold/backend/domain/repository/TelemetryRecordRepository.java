package com.pharmacold.backend.domain.repository;

import com.pharmacold.backend.domain.model.TelemetryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelemetryRecordRepository extends JpaRepository<TelemetryRecord, Long> {

    Page<TelemetryRecord> findByRelatedShipmentIdOrderByTimestampDesc(Long shipmentId, Pageable pageable);

    List<TelemetryRecord> findTop50ByRelatedShipmentIdOrderByTimestampDesc(Long shipmentId);
}
