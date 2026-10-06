package com.pharmacold.backend.domain.repository;

import com.pharmacold.backend.domain.enums.ShipmentStatus;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.model.Shipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @EntityGraph(attributePaths = {"assignedSensor"})
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(ShipmentStatus status);

    boolean existsByTrackingNumber(String trackingNumber);
}
