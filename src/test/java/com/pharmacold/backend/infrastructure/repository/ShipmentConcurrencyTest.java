package com.pharmacold.backend.infrastructure.repository;

import com.pharmacold.backend.domain.model.SensorDevice;
import com.pharmacold.backend.domain.model.Shipment;
import com.pharmacold.backend.domain.repository.SensorDeviceRepository;
import com.pharmacold.backend.domain.repository.ShipmentRepository;
import com.pharmacold.backend.infrastructure.config.JpaAuditingConfig;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pharmacold.backend.domain.model.SensorDeviceFixture.*;
import static com.pharmacold.backend.domain.model.ShipmentFixture.getShipmentToSave;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ShipmentConcurrencyTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private SensorDeviceRepository sensorDeviceRepository;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
        shipmentRepository.deleteAll();
        sensorDeviceRepository.deleteAll();
    }

    @DisplayName("Should throw exceptions when a shipment is updated concurrently")
    @RepeatedTest(value = 20, name = RepeatedTest.LONG_DISPLAY_NAME)
    void shouldThrowException_whenUpdateConcurrently() throws InterruptedException {
        //ARRANGE
        Shipment toSaveShipment = getShipmentToSave();
        Shipment createdShipment = shipmentRepository.saveAndFlush(toSaveShipment);
        SensorDevice sensor = sensorDeviceRepository.saveAndFlush(getSensorToSave());
        Long shipmentId = createdShipment.getId();

        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticError = new AtomicInteger(0);
        AtomicInteger unexpectedError = new AtomicInteger(0);

        //ACT
        executorService.submit(() -> {
            try {
                Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow();
                startSignal.await();
                shipment.startShipment(sensor);
                shipmentRepository.saveAndFlush(shipment);
                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                optimisticError.incrementAndGet();
            } catch (Exception e) {
                unexpectedError.incrementAndGet();
            } finally {
                doneSignal.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow();
                startSignal.await();
                shipment.cancel();
                shipmentRepository.saveAndFlush(shipment);
                successCount.incrementAndGet();
            } catch (OptimisticLockingFailureException e) {
                optimisticError.incrementAndGet();
            } catch (Exception e) {
                unexpectedError.incrementAndGet();
            } finally {
                doneSignal.countDown();
            }
        });

        startSignal.countDown();

        boolean completed = doneSignal.await(5, TimeUnit.SECONDS);

        //ASSERT
        assertThat(completed).isTrue();
        assertThat(successCount).hasValue(1);
        assertThat(unexpectedError).hasValue(0);
        assertThat(optimisticError).hasValue(1);

        Shipment updatedShipment = shipmentRepository.findById(shipmentId).orElseThrow();
        assertThat(updatedShipment.getVersion()).isEqualTo(1L);
    }
}
