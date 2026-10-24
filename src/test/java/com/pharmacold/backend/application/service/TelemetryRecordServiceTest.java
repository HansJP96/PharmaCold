package com.pharmacold.backend.application.service;

import com.pharmacold.backend.application.dto.request.CreateTelemetryRecordRequest;
import com.pharmacold.backend.application.dto.response.TelemetryRecordResponse;
import com.pharmacold.backend.application.mapper.TelemetryRecordMapper;
import com.pharmacold.backend.application.mapper.TelemetryRecordMapperImpl;
import com.pharmacold.backend.domain.enums.SensorStatus;
import com.pharmacold.backend.domain.enums.ShipmentStatus;
import com.pharmacold.backend.domain.exception.InconsistentTelemetrySensorException;
import com.pharmacold.backend.domain.exception.InvalidSensorStateException;
import com.pharmacold.backend.domain.exception.InvalidShipmentStateException;
import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.domain.model.TelemetryRecord;
import com.pharmacold.backend.domain.repository.TelemetryRecordRepository;
import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.pharmacold.backend.domain.model.SensorDeviceFixture.getAssignedSensor;
import static com.pharmacold.backend.domain.model.SensorDeviceFixture.getSensorWithStatus;
import static com.pharmacold.backend.domain.model.ShipmentFixture.getInTransitShipmentWithSensor;
import static com.pharmacold.backend.domain.model.ShipmentFixture.getShipmentWithStatus;
import static com.pharmacold.backend.domain.model.TelemetryRecordFixture.getCreateTelemetryRequest;
import static com.pharmacold.backend.domain.model.ThermalExcursionFixture.getValidThermalExcursion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TelemetryRecordServiceTest {

    @Mock
    private TelemetryRecordRepository telemetryRecordRepository;

    @Mock
    private SensorDeviceService sensorDeviceService;

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private ThermalExcursionService thermalExcursionService;

    @InjectMocks
    private TelemetryRecordService telemetryRecordService;

    @SuppressWarnings("unused")
    @Spy
    private final TelemetryRecordMapper telemetryRecordMapper = new TelemetryRecordMapperImpl();

    @Captor
    private ArgumentCaptor<TelemetryRecord> telemetryRecordArgumentCaptor;

    @Nested
    @DisplayName("Scenarios: Create telemetry record")
    class CreateTelemetryRecordTest {

        @Test
        @DisplayName("Should create a not risked telemetry record successfully when a valid request arrive")
        public void shouldCreateNotRiskedTelemetryRecordSuccessfully_whenAValidRequestArrive() {
            //ARRANGE
            CreateTelemetryRecordRequest request = getCreateTelemetryRequest(new BigDecimal("5"));
            Shipment existingShipment = getInTransitShipmentWithSensor(getAssignedSensor());
            when(shipmentService.findShipmentByTrackingNumber(request.shipmentTrackingNumber())).thenReturn(existingShipment);
            when(sensorDeviceService.findSensorDeviceBySerialNumber(request.sensorSerialNumber())).thenReturn(existingShipment.getAssignedSensor());
            when(telemetryRecordRepository.save(any(TelemetryRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            //ACT
            TelemetryRecordResponse response = telemetryRecordService.createTelemetryRecord(request);

            //ASSERT
            verify(thermalExcursionService, never()).generateThermalExcursionRecord(request.temperatureCelsius(), request.timestamp(), existingShipment);
            verify(telemetryRecordRepository).save(telemetryRecordArgumentCaptor.capture());
            assertThat(response).isNotNull().satisfies(res -> {
                assertThat(res.temperatureCelsius()).isEqualByComparingTo(request.temperatureCelsius());
                assertThat(res.isExcursion()).isEqualTo(false);
                assertThat(res.timestamp()).isEqualTo(request.timestamp());
            });
        }


        @Test
        @DisplayName("Should create a risked telemetry record successfully when a valid request arrive")
        public void shouldCreateRiskedTelemetryRecordSuccessfully_whenAValidRequestArrive() {
            //ARRANGE
            CreateTelemetryRecordRequest request = getCreateTelemetryRequest(new BigDecimal("40"));
            Shipment existingShipment = getInTransitShipmentWithSensor(getAssignedSensor());
            when(shipmentService.findShipmentByTrackingNumber(request.shipmentTrackingNumber())).thenReturn(existingShipment);
            when(sensorDeviceService.findSensorDeviceBySerialNumber(request.sensorSerialNumber())).thenReturn(existingShipment.getAssignedSensor());
            when(thermalExcursionService.generateThermalExcursionRecord(any(), any(), any(Shipment.class))).thenReturn(getValidThermalExcursion());
            when(telemetryRecordRepository.save(any(TelemetryRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            //ACT
            TelemetryRecordResponse response = telemetryRecordService.createTelemetryRecord(request);

            //ASSERT
            verify(thermalExcursionService, times(1)).generateThermalExcursionRecord(
                    request.temperatureCelsius(), request.timestamp(), existingShipment
            );
            verify(telemetryRecordRepository).save(telemetryRecordArgumentCaptor.capture());
            assertThat(response).isNotNull().satisfies(res -> {
                assertThat(res.temperatureCelsius()).isEqualByComparingTo(request.temperatureCelsius());
                assertThat(res.isExcursion()).isEqualTo(true);
                assertThat(res.timestamp()).isEqualTo(request.timestamp());
            });
        }

        @ParameterizedTest(name = "Should throw InvalidShipmentStateException when trying to register a telemetry record with an invalid shipment status {0}")
        @EnumSource(
                value = ShipmentStatus.class,
                mode = EnumSource.Mode.EXCLUDE,
                names = {"IN_TRANSIT"}
        )
        public void shouldThrowInvalidShipmentStateException_whenShipmentIsInInvalidStatusForTelemtryRecord(ShipmentStatus status) {
            //ARRANGE
            CreateTelemetryRecordRequest request = getCreateTelemetryRequest(new BigDecimal("5"));
            Shipment existingShipment = getShipmentWithStatus(status);
            when(shipmentService.findShipmentByTrackingNumber(request.shipmentTrackingNumber())).thenReturn(existingShipment);
            when(sensorDeviceService.findSensorDeviceBySerialNumber(request.sensorSerialNumber())).thenReturn(existingShipment.getAssignedSensor());

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(() -> telemetryRecordService.createTelemetryRecord(request));

            //ASSERT
            ex.isInstanceOfSatisfying(InvalidShipmentStateException.class, e -> {
                assertThat(e.getTrackingNumber()).isEqualTo(request.shipmentTrackingNumber());
                assertThat(e.getCurrentState()).isEqualTo(status);
            });
            verify(thermalExcursionService, never()).generateThermalExcursionRecord(request.temperatureCelsius(), request.timestamp(), existingShipment);
            verify(telemetryRecordRepository, never()).save(any(TelemetryRecord.class));
        }

        @ParameterizedTest(name = "Should throw InconsistentTelemetrySensorException when the reported telemetry sensor does not match the assigned sensor for shipment by {2}")
        @MethodSource("com.pharmacold.backend.domain.model.ShipmentFixture#getInconsistentShipmentSensor")
        public void shouldThrowInconsistentTelemetrySensorException_whenReportedTelemetryMismatchReportingSensor(Shipment existingShipment, String expected, String cause) {
            //ARRANGE
            CreateTelemetryRecordRequest request = getCreateTelemetryRequest(new BigDecimal("5"));
            SensorDevice sensorQueried = getAssignedSensor();
            when(shipmentService.findShipmentByTrackingNumber(request.shipmentTrackingNumber())).thenReturn(existingShipment);
            when(sensorDeviceService.findSensorDeviceBySerialNumber(request.sensorSerialNumber())).thenReturn(sensorQueried);

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(() -> telemetryRecordService.createTelemetryRecord(request));

            //ASSERT
            ex.isInstanceOfSatisfying(InconsistentTelemetrySensorException.class, e -> {
                assertThat(e.getTelemetrySensor()).isEqualTo(request.sensorSerialNumber());
                assertThat(e.getShipmentAssignedSensor()).isEqualTo(expected);
            });
            verify(thermalExcursionService, never()).generateThermalExcursionRecord(request.temperatureCelsius(), request.timestamp(), existingShipment);
            verify(telemetryRecordRepository, never()).save(any(TelemetryRecord.class));
        }


        @ParameterizedTest(name = "Should throw InvalidSensorStateException when the sensor is in status {0} to register a telemetry record")
        @EnumSource(
                value = SensorStatus.class,
                mode = EnumSource.Mode.EXCLUDE,
                names = {"ASSIGNED"}
        )
        public void shouldThrowInvalidSensorStateException_whenSensorIsInValidStatusForTelemetryRecord(SensorStatus status) {
            //ARRANGE
            CreateTelemetryRecordRequest request = getCreateTelemetryRequest(new BigDecimal("5"));
            Shipment existingShipment = getInTransitShipmentWithSensor(getSensorWithStatus(status));
            when(shipmentService.findShipmentByTrackingNumber(request.shipmentTrackingNumber())).thenReturn(existingShipment);
            when(sensorDeviceService.findSensorDeviceBySerialNumber(request.sensorSerialNumber())).thenReturn(existingShipment.getAssignedSensor());

            //ACT
            AbstractThrowableAssert<?, ?> ex = assertThatThrownBy(() -> telemetryRecordService.createTelemetryRecord(request));

            //ASSERT
            ex.isInstanceOfSatisfying(InvalidSensorStateException.class, e -> {
                assertThat(e.getSerialNumber()).isEqualTo(request.sensorSerialNumber());
                assertThat(e.getCurrentState()).isEqualTo(status);
            });
            verify(thermalExcursionService, never()).generateThermalExcursionRecord(request.temperatureCelsius(), request.timestamp(), existingShipment);
            verify(telemetryRecordRepository, never()).save(any(TelemetryRecord.class));
        }
    }
}
