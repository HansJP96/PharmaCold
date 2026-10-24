package com.pharmacold.backend.application.service;

import com.pharmacold.backend.domain.enums.ExcursionSeverity;
import com.pharmacold.backend.domain.enums.ExcursionStatus;
import com.pharmacold.backend.domain.enums.ShipmentStatus;
import com.pharmacold.backend.domain.event.ThermalExcursionDetectedEvent;
import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.domain.model.ThermalExcursion;
import com.pharmacold.backend.domain.repository.ThermalExcursionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;

import static com.pharmacold.backend.domain.model.ShipmentFixture.getInTransitShipmentWithTempLimits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ThermalExcursionServiceTest {


    @Mock
    private ThermalExcursionRepository thermalExcursionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ThermalExcursionService thermalExcursionService;

    @Captor
    private ArgumentCaptor<ThermalExcursionDetectedEvent> excursionEventArgumentCaptor;

    @Nested
    @DisplayName("Scenarios: Generate Excursion Record")
    class GenerateExcursionTest {

        @DisplayName("Should create a successfully thermal excursion record in warning status when telemetry service notifies")
        @ParameterizedTest
        @CsvSource({"10,-2,15", "10,-10,10", "14.49,0,9.50"})
        public void shouldCreateSuccessfullyThermalExcursionRecordInWarningStatus_whenTelemetryServiceNotifies(
                BigDecimal recordedTemp, BigDecimal minTemp, BigDecimal maxTemp) {
            //ARRANGE
            Instant timestamp = Instant.now();
            Shipment existingShipment = getInTransitShipmentWithTempLimits(minTemp, maxTemp);
            when(thermalExcursionRepository.save(any(ThermalExcursion.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(eventPublisher).publishEvent(any(ThermalExcursionDetectedEvent.class));

            //ACT
            ThermalExcursion thermalExcursion = thermalExcursionService.generateThermalExcursionRecord(recordedTemp, timestamp, existingShipment);

            //ASSERT
            assertThat(existingShipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
            assertThat(thermalExcursion.getSeverity()).isEqualTo(ExcursionSeverity.WARNING);
            assertThat(thermalExcursion.getStatus()).isEqualTo(ExcursionStatus.OPEN);
            assertThat(thermalExcursion.getDetectedAt()).isEqualTo(timestamp);
            verify(thermalExcursionRepository).save(thermalExcursion);
            verify(eventPublisher, times(1)).publishEvent(excursionEventArgumentCaptor.capture());
            ThermalExcursionDetectedEvent event = excursionEventArgumentCaptor.getValue();
            assertThat(event.trackingNumber()).isEqualTo(existingShipment.getTrackingNumber());
            assertThat(event.severity()).isEqualTo(thermalExcursion.getSeverity());
            assertThat(event.detectedAt()).isEqualTo(thermalExcursion.getDetectedAt());
            assertThat(event.recordedTemperature()).isEqualTo(recordedTemp);
            assertThat(event.minTempCelsius()).isEqualTo(existingShipment.getMinTempCelsius());
            assertThat(event.maxTempCelsius()).isEqualTo(existingShipment.getMaxTempCelsius());
        }

        @DisplayName("Should create a successfully thermal excursion record in critical status when telemetry service notifies")
        @ParameterizedTest
        @CsvSource({"21,-2,15", "-15.00,-10,10", "14.50,0,9.50"})
        public void shouldCreateSuccessfullyThermalExcursionRecordInCriticalStatus_whenTelemetryServiceNotifies(
                BigDecimal recordedTemp, BigDecimal minTemp, BigDecimal maxTemp) {
            //ARRANGE
            Instant timestamp = Instant.now();
            Shipment existingShipment = getInTransitShipmentWithTempLimits(minTemp, maxTemp);
            when(thermalExcursionRepository.save(any(ThermalExcursion.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            doNothing().when(eventPublisher).publishEvent(any(ThermalExcursionDetectedEvent.class));

            //ACT
            ThermalExcursion thermalExcursion = thermalExcursionService.generateThermalExcursionRecord(recordedTemp, timestamp, existingShipment);

            //ASSERT
            assertThat(existingShipment.getStatus()).isEqualTo(ShipmentStatus.COMPROMISED);
            assertThat(thermalExcursion.getSeverity()).isEqualTo(ExcursionSeverity.CRITICAL);
            assertThat(thermalExcursion.getStatus()).isEqualTo(ExcursionStatus.OPEN);
            assertThat(thermalExcursion.getDetectedAt()).isEqualTo(timestamp);
            verify(thermalExcursionRepository).save(thermalExcursion);
            verify(eventPublisher, times(1)).publishEvent(excursionEventArgumentCaptor.capture());
            ThermalExcursionDetectedEvent event = excursionEventArgumentCaptor.getValue();
            assertThat(event.trackingNumber()).isEqualTo(existingShipment.getTrackingNumber());
            assertThat(event.severity()).isEqualTo(thermalExcursion.getSeverity());
            assertThat(event.detectedAt()).isEqualTo(thermalExcursion.getDetectedAt());
            assertThat(event.recordedTemperature()).isEqualTo(recordedTemp);
            assertThat(event.minTempCelsius()).isEqualTo(existingShipment.getMinTempCelsius());
            assertThat(event.maxTempCelsius()).isEqualTo(existingShipment.getMaxTempCelsius());

        }

        @DisplayName("Should throw an exception when an error occurred saving a Thermal Excursion")
        @Test
        public void shouldThrowException_whenErrorOccurredSavingThermalExcursion() {
            //ARRANGE
            Instant timestamp = Instant.now();
            Shipment existingShipment = getInTransitShipmentWithTempLimits(
                    new BigDecimal("1.00"),
                    new BigDecimal("20.00")
            );
            doThrow(new DataIntegrityViolationException("DB Error")).when(thermalExcursionRepository).save(any(ThermalExcursion.class));

            //ACT
            assertThatThrownBy(
                    () -> thermalExcursionService.generateThermalExcursionRecord(
                            new BigDecimal("15.00"),
                            timestamp,
                            existingShipment
                    )
            ).isInstanceOf(DataIntegrityViolationException.class);

            //ASSERT
            verify(eventPublisher, never()).publishEvent(any(ThermalExcursionDetectedEvent.class));
        }
    }

}
