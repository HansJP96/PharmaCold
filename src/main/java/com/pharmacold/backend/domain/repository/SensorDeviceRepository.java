package com.pharmacold.backend.domain.repository;

import com.pharmacold.backend.domain.enums.SensorStatus;
import com.pharmacold.backend.domain.model.SensorDevice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SensorDeviceRepository extends JpaRepository<SensorDevice, Long> {

    Optional<SensorDevice> findBySerialNumber(String serialNumber);

    Slice<SensorDevice> findByStatus(SensorStatus status, Pageable pageable);

    boolean existsBySerialNumber(String serialNumber);
}
