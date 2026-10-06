package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.ShipmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "shipments")
public class Shipment extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @Column(name = "origin_facility", nullable = false)
    private String originFacility;

    @Column(name = "destination_facility", nullable = false)
    private String destinationFacility;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "batch_number", nullable = false)
    private String batchNumber;

    @Digits(integer = 3, fraction = 2, message = "Minimum temperature must have at most 3 integer and 2 decimal places")
    @Column(name = "min_temp_celsius", nullable = false)
    private BigDecimal minTempCelsius;

    @Digits(integer = 3, fraction = 2, message = "Maximum temperature must have at most 3 integer and 2 decimal places")
    @Column(name = "max_temp_celsius", nullable = false)
    private BigDecimal maxTempCelsius;

    @Column(name = "max_allowed_excursion_minutes", nullable = false)
    private Integer maxAllowedExcursionMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private SensorDevice assignedSensor;
}
