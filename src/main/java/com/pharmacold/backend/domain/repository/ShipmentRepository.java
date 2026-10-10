package com.pharmacold.backend.domain.repository;

import com.pharmacold.backend.domain.enums.ShipmentStatus;
import com.pharmacold.backend.domain.model.Shipment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    @EntityGraph(attributePaths = {"assignedSensor"})
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Slice<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    boolean existsByTrackingNumber(String trackingNumber);
}
