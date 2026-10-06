package com.pharmacold.backend.domain.repository;

import com.pharmacold.backend.domain.enums.SensorStatus;
import com.pharmacold.backend.domain.model.SensorDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDeviceRepository extends JpaRepository<SensorDevice, Long> {

    Optional<SensorDevice> findBySerialNumber(String serialNumber);

    List<SensorDevice> findByStatus(SensorStatus status);

    boolean existsBySerialNumber(String serialNumber);
}
