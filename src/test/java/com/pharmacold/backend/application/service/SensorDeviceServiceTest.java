package com.pharmacold.backend.application.service;

import com.pharmacold.backend.application.dto.request.CreateSensorDeviceRequest;
import com.pharmacold.backend.application.dto.response.SensorDeviceResponse;
import com.pharmacold.backend.application.mapper.SensorDeviceMapper;
import com.pharmacold.backend.application.mapper.SensorDeviceMapperImpl;
import com.pharmacold.backend.domain.enums.SensorStatus;
import com.pharmacold.backend.domain.exception.DuplicateResourceException;
import com.pharmacold.backend.domain.exception.InvalidSensorStateException;
import com.pharmacold.backend.domain.exception.ResourceNotFoundException;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.repository.SensorDeviceRepository;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SensorDeviceServiceTest {

    @Mock
    private SensorDeviceRepository sensorDeviceRepository;

    @SuppressWarnings("unused")
    @Spy
    private SensorDeviceMapper sensorDeviceMapper = new SensorDeviceMapperImpl();

    @InjectMocks
    private SensorDeviceService sensorDeviceService;

    @Captor
    private ArgumentCaptor<SensorDevice> sensorDeviceCaptor;

    @Nested
    @DisplayName("Scenarios: Register a new Sensor")
    class RegisterSensorTest {

        @Test
        @DisplayName("Should create a new sensor in state available when a sensor is registered")
        public void shouldCreateNewSensorAvailable_whenSensorIsRegistered() {
            //ARRANGE
            CreateSensorDeviceRequest sensorRequest = getSensorRequest();
            when(sensorDeviceRepository.existsBySerialNumber(sensorRequest.serialNumber())).thenReturn(false);
            when(sensorDeviceRepository.save(any(SensorDevice.class))).thenAnswer(invocation -> {
                SensorDevice insideSensor = invocation.getArgument(0);
                insideSensor.setId(1L);
                return insideSensor;
            });

            //ACT
            SensorDeviceResponse response = sensorDeviceService.registerSensorDevice(sensorRequest);

            //ASSERT
            verify(sensorDeviceRepository).save(sensorDeviceCaptor.capture());
            assertThat(response).isNotNull().satisfies(sensorResponse ->
                    {
                        assertThat(sensorResponse.id()).isEqualTo(sensorDeviceCaptor.getValue().getId());
                        assertThat(sensorResponse.serialNumber()).isEqualTo(sensorRequest.serialNumber());
                        assertThat(sensorResponse.model()).isEqualTo(sensorRequest.model());
                        assertThat(sensorResponse.batteryPercentage()).isEqualTo(sensorRequest.batteryPercentage());
                        assertThat(sensorResponse.status()).isEqualTo(SensorStatus.AVAILABLE);
                        assertThat(sensorResponse.lastCalibratedAt()).isNull();
                    }
            );
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when a sensor with a specified serial number already exists")
        public void shouldThrowException_whenSensorAlreadyExists() {
            //ARRANGE
            CreateSensorDeviceRequest sensorRequest = getSensorRequest();
            when(sensorDeviceRepository.existsBySerialNumber(sensorRequest.serialNumber())).thenReturn(true);

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(() -> sensorDeviceService.registerSensorDevice(sensorRequest));

            //THEN
            ex.isInstanceOfSatisfying(DuplicateResourceException.class, e -> {
                assertThat(e.getResourceName()).isEqualTo(SensorDevice.class.getSimpleName());
                assertThat(e.getFieldName()).isEqualTo("serialNumber");
                assertThat(e.getFieldValue()).isEqualTo(sensorRequest.serialNumber());
            });
            verify(sensorDeviceRepository, never()).save(any(SensorDevice.class));
        }
    }

    @Nested
    @DisplayName("Scenarios: Calibrating a Sensor")
    class CalibrationSensorTest {

        @ParameterizedTest(name = "Should update sensor state to AVAILABLE when the sensor was calibrated from state [{0}]")
        @EnumSource(
                mode = EnumSource.Mode.EXCLUDE,
                value = SensorStatus.class,
                names = {"ASSIGNED", "DECOMMISSIONED"}
        )
        public void shouldUpdateStateToAvailable_whenSensorIsCalibratedInState(SensorStatus sensorStatus) {
            //ARRANGE
            SensorDevice existingSensor = getCreatedSensorWithStatus(sensorStatus);
            when(sensorDeviceRepository.findById(existingSensor.getId())).thenReturn(Optional.of(existingSensor));
            when(sensorDeviceRepository.save(any(SensorDevice.class))).thenReturn(existingSensor);

            //ACT
            Instant momentBeforeAct = Instant.now();
            SensorDeviceResponse response = sensorDeviceService.calibrateSensorDevice(existingSensor.getId());

            //ASSERT
            Instant momentAfterAct = Instant.now();
            assertThat(response).isNotNull().satisfies(sensorResponse ->
                    {
                        assertThat(sensorResponse.id()).isNotNull().isEqualTo(existingSensor.getId());
                        assertThat(sensorResponse.status()).isEqualTo(SensorStatus.AVAILABLE);
                        assertThat(sensorResponse.lastCalibratedAt())
                                .isNotNull()
                                .isAfterOrEqualTo(momentBeforeAct)
                                .isBeforeOrEqualTo(momentAfterAct);
                    }
            );
            verify(sensorDeviceRepository).save(existingSensor);
        }

        @ParameterizedTest(name = "Should throw InvalidSensorStateException when trying to calibrate a sensor in status [{0}]")
        @EnumSource(
                mode = EnumSource.Mode.INCLUDE,
                value = SensorStatus.class,
                names = {"ASSIGNED", "DECOMMISSIONED"}
        )
        public void shouldThrowException_whenCalibrateSensorInStatus(SensorStatus sensorStatus) {
            //ARRANGE
            SensorDevice existingSensor = getCreatedSensorWithStatus(sensorStatus);
            when(sensorDeviceRepository.findById(existingSensor.getId())).thenReturn(Optional.of(existingSensor));

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(() -> sensorDeviceService.calibrateSensorDevice(existingSensor.getId()));

            //THEN
            ex.isInstanceOfSatisfying(
                    InvalidSensorStateException.class,
                    e -> assertThat(e.getCurrentState()).isEqualTo(sensorStatus)
            );
            verify(sensorDeviceRepository, never()).save(existingSensor);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when a sensor with a specified id does not exists")
        public void shouldThrowException_whenSensorDoesNotExists() {
            //ARRANGE
            Long id = 1L;
            when(sensorDeviceRepository.findById(id)).thenReturn(Optional.empty());

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(() -> sensorDeviceService.calibrateSensorDevice(id));

            //THEN
            ex.isInstanceOfSatisfying(ResourceNotFoundException.class, e -> {
                assertThat(e.getResourceName()).isEqualTo(SensorDevice.class.getSimpleName());
                assertThat(e.getFieldName()).isEqualTo("id");
                assertThat(e.getFieldValue()).isEqualTo(id);
            });
            verify(sensorDeviceRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Scenarios: Sensor updates from ShipmentServices")
    class ShipmentSensorUpdatesTest {

        @Test
        @DisplayName("Should update sensor state when the sensor is assigned to a shipment")
        public void shouldUpdateSensorState_whenSensorIsAssignedToShipment() {
            //ARRANGE
            SensorDevice sensorDevice = getCreatedSensorWithStatus(SensorStatus.AVAILABLE);

            //ACT
            sensorDeviceService.assignSensorToShipment(sensorDevice);

            //ASSERT
            assertThat(sensorDevice.getStatus()).isEqualTo(SensorStatus.ASSIGNED);
            verify(sensorDeviceRepository).save(sensorDevice);
        }

        @ParameterizedTest(name = "Should throw SensorNotAvailableException when the sensor is not AVAILABLE to assign to a shipment")
        @EnumSource(
                mode = EnumSource.Mode.EXCLUDE,
                value = SensorStatus.class,
                names = {"AVAILABLE"}
        )
        public void shouldThrowException_whenSensorIsNotAvailableToShipment(SensorStatus sensorStatus) {
            //ARRANGE
            SensorDevice existingSensor = getCreatedSensorWithStatus(sensorStatus);

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(
                    () -> sensorDeviceService.assignSensorToShipment(existingSensor)
            );

            //ASSERT
            ex.isInstanceOfSatisfying(InvalidSensorStateException.class, e -> {
                assertThat(e.getSerialNumber()).isEqualTo(existingSensor.getSerialNumber());
                assertThat(e.getCurrentState()).isEqualTo(existingSensor.getStatus());
            });
            verify(sensorDeviceRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should update sensor state when the sensor is released from a shipment (delivered)")
        public void shouldUpdateSensorState_whenSensorIsReleasedFromShipment() {
            //ARRANGE
            SensorDevice existingSensor = getCreatedSensorWithStatus(SensorStatus.ASSIGNED);

            //ACT
            sensorDeviceService.markSensorForCalibration(existingSensor);

            //ASSERT
            assertThat(existingSensor.getStatus()).isEqualTo(SensorStatus.CALIBRATION_REQUIRED);
            verify(sensorDeviceRepository).save(existingSensor);
        }
    }

    private CreateSensorDeviceRequest getSensorRequest() {
        return new CreateSensorDeviceRequest("IOT-SNS-00001", "Samsung XPA", 100);
    }

    private SensorDevice getCreatedSensorWithStatus(SensorStatus sensorStatus) {
        return SensorDevice.builder()
                .id(2L)
                .serialNumber("IOT-SNS-00001")
                .model("Samsung XPA")
                .batteryPercentage(100)
                .status(sensorStatus)
                .lastCalibratedAt(null)
                .build();
    }
}
