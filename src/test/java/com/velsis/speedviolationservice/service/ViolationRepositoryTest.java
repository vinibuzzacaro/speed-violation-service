package com.velsis.speedviolationservice.service;

import com.velsis.speedviolationservice.enums.OriginType;
import com.velsis.speedviolationservice.enums.ViolationSeverity;
import com.velsis.speedviolationservice.model.Violation;
import com.velsis.speedviolationservice.repository.ViolationRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationRepositoryTest {

    private final ViolationRepository repository = new ViolationRepository();

    private Violation sampleViolation(String plate) {
        return new Violation(plate, "RAD-CWB-001", 92, 85, 60, 41.67,
            ViolationSeverity.SERIOUS, OriginType.FIXED, Instant.now(), Instant.now());
    }

    @Test
    void returnsEmptyListWhenPlateHasNoViolations() {
        assertThat(repository.findByLicensePlate("ZZZ9999")).isEmpty();
    }

    @Test
    void storesAndRetrievesViolationsByPlate() {
        repository.save(sampleViolation("ABC1D23"));
        repository.save(sampleViolation("ABC1D23"));

        List<Violation> violations = repository.findByLicensePlate("ABC1D23");

        assertThat(violations).hasSize(2);
    }

    @Test
    void supportsConcurrentWritesToTheSamePlateWithoutLosingRecords() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                repository.save(sampleViolation("CONCURRENT1"));
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(repository.findByLicensePlate("CONCURRENT1")).hasSize(threadCount);
    }
}
