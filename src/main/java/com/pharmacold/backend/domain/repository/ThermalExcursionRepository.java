package com.pharmacold.backend.domain.repository;

import com.pharmacold.backend.domain.enums.ExcursionStatus;
import com.pharmacold.backend.domain.model.ThermalExcursion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThermalExcursionRepository extends JpaRepository<ThermalExcursion, Long> {

    List<ThermalExcursion> findByCompromisedShipmentId(Long shipmentId);

    List<ThermalExcursion> findByStatus(ExcursionStatus status);

    long countByCompromisedShipmentIdAndStatus(Long shipmentId, ExcursionStatus status);
}
