package com.pharmacold.backend.application.service;

import com.pharmacold.backend.application.dto.request.CreateShipmentRequest;
import com.pharmacold.backend.application.dto.response.ShipmentResponse;
import com.pharmacold.backend.application.mapper.ShipmentMapper;
import com.pharmacold.backend.application.mapper.ShipmentMapperImpl;
import com.pharmacold.backend.domain.enums.SensorStatus;
import com.pharmacold.backend.domain.enums.ShipmentStatus;
import com.pharmacold.backend.domain.exception.*;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.domain.repository.ShipmentRepository;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @SuppressWarnings("unused")
    @Spy
    private ShipmentMapper shipmentMapper = new ShipmentMapperImpl();

    @InjectMocks
    private ShipmentService shipmentService;

    @Mock
    private SensorDeviceService sensorDeviceService;

    @Captor
    private ArgumentCaptor<Shipment> shipmentCaptor;

    @Nested
    @DisplayName("Scenarios: Creating Shipment")
    class CreateShipmentTest {

        @Test
        @DisplayName("Should create a new shipment in DB when shipment data are received")
        public void shouldCreateNewShipment_whenShipmentDataReceived() {
            //ARRANGE
            CreateShipmentRequest shipmentRequest = getShipmentRequest();
            when(shipmentRepository.existsByTrackingNumber(shipmentRequest.trackingNumber())).thenReturn(false);
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment shipmentCreated = invocation.getArgument(0);
                shipmentCreated.setId(1L);
                return shipmentCreated;
            });

            //ACT
            ShipmentResponse shipmentResponse = shipmentService.createShipment(shipmentRequest);

            //ASSERT
            verify(shipmentRepository).save(shipmentCaptor.capture());
            assertThat(shipmentResponse).isNotNull().satisfies(response -> {
                assertThat(response.id()).isEqualTo(shipmentCaptor.getValue().getId());
                assertThat(response.trackingNumber()).isEqualTo(shipmentRequest.trackingNumber());
                assertThat(response.productName()).isEqualTo(shipmentRequest.productName());
                assertThat(response.originFacility()).isEqualTo(shipmentRequest.originFacility());
                assertThat(response.destinationFacility()).isEqualTo(shipmentRequest.destinationFacility());
                assertThat(response.batchNumber()).isEqualTo(shipmentRequest.batchNumber());
                assertThat(response.status()).isEqualTo(ShipmentStatus.CREATED);
                assertThat(response.minTempCelsius()).isEqualByComparingTo(shipmentRequest.minTempCelsius());
                assertThat(response.maxTempCelsius()).isEqualByComparingTo(shipmentRequest.maxTempCelsius());
                assertThat(response.maxAllowedExcursionMinutes()).isEqualTo(shipmentRequest.maxAllowedExcursionMinutes());
                assertThat(response.assignedSensorId()).isNull();
            });
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when a shipment with a specified tracking number already exists")
        public void shouldThrowException_whenShipmentWithTrackingNumberAlreadyExists() {
            //ARRANGE
            CreateShipmentRequest shipmentRequest = getShipmentRequest();
            when(shipmentRepository.existsByTrackingNumber(shipmentRequest.trackingNumber())).thenReturn(true);

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(() -> shipmentService.createShipment(shipmentRequest));

            //ASSERT
            ex.isInstanceOfSatisfying(DuplicateResourceException.class, e -> {
                assertThat(e.getResourceName()).isEqualTo(Shipment.class.getSimpleName());
                assertThat(e.getFieldName()).isEqualTo("trackingNumber");
                assertThat(e.getFieldValue()).isEqualTo(shipmentRequest.trackingNumber());
            });
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Nested
    @DisplayName("Scenarios: Starting Shipment")
    class StartShipmentTest {

        @Test
        @DisplayName("Should start a shipment successfully when the shipment is in the CREATED status")
        public void shouldStartShipment_whenShipmentIsCreated() {
            //ARRANGE
            Shipment shipment = getExistingShipmentWithStatusAndSensor(ShipmentStatus.CREATED, null);
            SensorDevice sensor = getExistingSensorWithStatus(SensorStatus.AVAILABLE);
            when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
            when(sensorDeviceService.findSensorDeviceBySerialNumber(sensor.getSerialNumber())).thenReturn(sensor);
            when(shipmentRepository.save(shipment)).thenReturn(shipment);

            //ACT
            ShipmentResponse shipmentResponse = shipmentService.startTransit(shipment.getId(), sensor.getSerialNumber());

            //ASSERT
            assertThat(shipmentResponse.status()).isEqualTo(ShipmentStatus.IN_TRANSIT);
            assertThat(shipmentResponse.assignedSensorId()).isNotNull().isEqualTo(sensor.getId());

            verify(shipmentRepository).save(shipment);
            verify(sensorDeviceService, times(1)).assignSensorToShipment(eq(sensor));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when a shipment with id does not exist")
        public void shouldThrowException_whenShipmentIdDoesNotExist() {
            //ARRANGE
            SensorDevice sensorDevice = getExistingSensorWithStatus(SensorStatus.AVAILABLE);
            Long shipmentId = 1L;
            when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(
                    () -> shipmentService.startTransit(shipmentId, sensorDevice.getSerialNumber())
            );

            //ASSERT
            ex.isInstanceOfSatisfying(ResourceNotFoundException.class, e -> {
                assertThat(e.getResourceName()).isEqualTo(Shipment.class.getSimpleName());
                assertThat(e.getFieldName()).isEqualTo("id");
                assertThat(e.getFieldValue()).isEqualTo(shipmentId);
            });
            verifyNoInteractions(sensorDeviceService);
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }

        @Test
        @DisplayName("Should not start the shipment when a sensor assign fails")
        public void shouldNotStartShipment_whenSensorIsNotAvailable() {
            //ARRANGE
            SensorDevice existingSensor = getExistingSensorWithStatus(SensorStatus.ASSIGNED);
            Shipment existingShipment = getExistingShipmentWithStatusAndSensor(ShipmentStatus.CREATED, existingSensor);
            when(shipmentRepository.findById(existingShipment.getId())).thenReturn(Optional.of(existingShipment));
            when(sensorDeviceService.findSensorDeviceBySerialNumber(existingSensor.getSerialNumber())).thenReturn(existingSensor);
            doThrow(new InvalidSensorStateException(existingSensor.getSerialNumber(), existingSensor.getStatus()))
                    .when(sensorDeviceService).assignSensorToShipment(existingSensor);

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(
                    () -> shipmentService.startTransit(existingShipment.getId(), existingSensor.getSerialNumber())
            );

            //ASSERT
            ex.isInstanceOfSatisfying(InvalidSensorStateException.class, e -> {
                assertThat(e.getSerialNumber()).isEqualTo(existingSensor.getSerialNumber());
                assertThat(e.getCurrentState()).isEqualTo(existingSensor.getStatus());
            });
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }

        @ParameterizedTest(name = "Should throw InvalidShipmentStateTransitionException when a shipment to transit is in a status [{0}]")
        @EnumSource(
                mode = EnumSource.Mode.EXCLUDE,
                value = ShipmentStatus.class,
                names = {"CREATED"}
        )
        public void shouldThrowException_whenShipmentToTransitIsInState(ShipmentStatus shipmentStatus) {
            //ARRANGE
            SensorDevice sensorDevice = getExistingSensorWithStatus(SensorStatus.AVAILABLE);
            Shipment existingShipment = getExistingShipmentWithStatusAndSensor(shipmentStatus, sensorDevice);
            when(shipmentRepository.findById(existingShipment.getId())).thenReturn(Optional.of(existingShipment));
            when(sensorDeviceService.findSensorDeviceBySerialNumber(sensorDevice.getSerialNumber())).thenReturn(sensorDevice);

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(
                    () -> shipmentService.startTransit(existingShipment.getId(), sensorDevice.getSerialNumber())
            );

            //ASSERT
            ex.isInstanceOfSatisfying(InvalidShipmentStateTransitionException.class, e -> {
                assertThat(e.getTargetState()).isEqualTo(ShipmentStatus.IN_TRANSIT);
                assertThat(e.getCurrentState()).isEqualTo(existingShipment.getStatus());
            });
//            verify(sensorDeviceService, never()).assignSensorToShipment(any(SensorDevice.class));
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Nested
    @DisplayName("Scenarios: Delivering Shipment")
    class DeliverShipmentTest {

        @Test
        @DisplayName("Should deliver the shipment successfully when the shipment is in status IN_TRANSIT")
        public void shouldDeliverShipmentSuccessfully_whenShipmentIsInStatusInTransit() {
            //ARRANGE
            Shipment shipment = getExistingShipmentWithStatusAndSensor(ShipmentStatus.IN_TRANSIT, getExistingSensorWithStatus(SensorStatus.AVAILABLE));
            when(shipmentRepository.findByTrackingNumber(shipment.getTrackingNumber())).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(shipment)).thenReturn(shipment);

            //ACT
            ShipmentResponse response = shipmentService.deliverShipment(shipment.getTrackingNumber());

            //ASSERT
            assertThat(response.status()).isEqualTo(ShipmentStatus.DELIVERED);
            assertThat(response.assignedSensorId()).isEqualTo(shipment.getAssignedSensor().getId());
            verify(sensorDeviceService, times(1)).markSensorForCalibration(shipment.getAssignedSensor());
            verify(shipmentRepository).save(shipment);
        }

        @ParameterizedTest(name = "Should throw InvalidShipmentStateTransitionException when a shipment to delivery is in a status [{0}]")
        @EnumSource(
                mode = EnumSource.Mode.EXCLUDE,
                value = ShipmentStatus.class,
                names = {"IN_TRANSIT"}
        )
        public void shouldThrowException_whenShipmentToDeliveryIsInState(ShipmentStatus shipmentStatus) {
            //ARRANGE
            Shipment existingShipment = getExistingShipmentWithStatusAndSensor(shipmentStatus, getExistingSensorWithStatus(SensorStatus.AVAILABLE));
            when(shipmentRepository.findByTrackingNumber(existingShipment.getTrackingNumber())).thenReturn(Optional.of(existingShipment));

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(
                    () -> shipmentService.deliverShipment(existingShipment.getTrackingNumber())
            );

            //ASSERT
            ex.isInstanceOfSatisfying(InvalidShipmentStateTransitionException.class, e -> {
                assertThat(e.getTargetState()).isEqualTo(ShipmentStatus.DELIVERED);
                assertThat(e.getCurrentState()).isEqualTo(existingShipment.getStatus());
            });
            verify(sensorDeviceService, never()).markSensorForCalibration(any(SensorDevice.class));
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when a shipment by tracking number does not exist")
        public void shouldThrowException_whenShipmentTrackingNumberDoesNotExist() {
            //ARRANGE
            SensorDevice sensorDevice = getExistingSensorWithStatus(SensorStatus.AVAILABLE);
            String trackingNumber = "IOT-SNS-00005";
            when(shipmentRepository.findByTrackingNumber(trackingNumber)).thenReturn(Optional.empty());

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(
                    () -> shipmentService.deliverShipment(trackingNumber)
            );

            //ASSERT
            ex.isInstanceOfSatisfying(ResourceNotFoundException.class, e -> {
                assertThat(e.getResourceName()).isEqualTo(Shipment.class.getSimpleName());
                assertThat(e.getFieldName()).isEqualTo("trackingNumber");
                assertThat(e.getFieldValue()).isEqualTo(trackingNumber);
            });
            verifyNoInteractions(sensorDeviceService);
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    @Nested
    @DisplayName("Scenarios: Cancelling Shipment")
    class CancelShipmentTest {

        @Test
        @DisplayName("Should cancel a shipment successfully when the shipment is in status CREATED")
        public void shouldCancelShipmentSuccessfully_whenShipmentIsInStatusCreated() {
            //ARRANGE
            Shipment shipment = getExistingShipmentWithStatusAndSensor(ShipmentStatus.CREATED, null);
            when(shipmentRepository.findByTrackingNumber(shipment.getTrackingNumber())).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(shipment)).thenReturn(shipment);

            //ACT
            ShipmentResponse response = shipmentService.cancelShipment(shipment.getTrackingNumber());

            //ASSERT
            assertThat(response.status()).isEqualTo(ShipmentStatus.CANCELLED);
            verify(shipmentRepository).save(shipment);
        }

        @ParameterizedTest(name = "Should throw InvalidShipmentStateTransitionException when a shipment to cancel is in a status [{0}]")
        @EnumSource(
                mode = EnumSource.Mode.EXCLUDE,
                value = ShipmentStatus.class,
                names = {"CREATED"}
        )
        public void shouldThrowException_whenShipmentToCancelIsInStatus(ShipmentStatus shipmentStatus) {
            //ARRANGE
            Shipment existingShipment = getExistingShipmentWithStatusAndSensor(shipmentStatus, null);
            when(shipmentRepository.findByTrackingNumber(existingShipment.getTrackingNumber())).thenReturn(Optional.of(existingShipment));

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(
                    () -> shipmentService.cancelShipment(existingShipment.getTrackingNumber())
            );

            //ASSERT
            ex.isInstanceOfSatisfying(InvalidShipmentStateTransitionException.class, e -> {
                assertThat(e.getTargetState()).isEqualTo(ShipmentStatus.CANCELLED);
                assertThat(e.getCurrentState()).isEqualTo(existingShipment.getStatus());
            });
            verify(shipmentRepository, never()).save(any(Shipment.class));
        }
    }

    private CreateShipmentRequest getShipmentRequest() {
        return CreateShipmentRequest.builder()
                .trackingNumber("PHARMA-2026-X89B")
                .productName("Gyossillycus")
                .originFacility("Pharma East")
                .destinationFacility("Pharma West")
                .batchNumber("15D2026")
                .minTempCelsius(new BigDecimal("2.00"))
                .maxTempCelsius(new BigDecimal("18.55"))
                .maxAllowedExcursionMinutes(2)
                .build();

    }

    private Shipment getExistingShipmentWithStatusAndSensor(ShipmentStatus shipmentStatus, SensorDevice sensor) {
        return Shipment.builder()
                .id(1L)
                .trackingNumber("PHARMA-2026-X89B")
                .productName("Gyossillycus")
                .originFacility("Pharma East")
                .destinationFacility("Pharma West")
                .batchNumber("15D2026")
                .status(shipmentStatus)
                .minTempCelsius(new BigDecimal("1.00"))
                .maxTempCelsius(new BigDecimal("22.00"))
                .maxAllowedExcursionMinutes(1)
                .assignedSensor(sensor)
                .build();

    }

    private SensorDevice getExistingSensorWithStatus(SensorStatus status) {
        return SensorDevice.builder()
                .id(1L)
                .serialNumber("IOT-SNS-00001")
                .model("Samsung XPA")
                .batteryPercentage(100)
                .status(status)
                .lastCalibratedAt(null)
                .build();
    }
}
