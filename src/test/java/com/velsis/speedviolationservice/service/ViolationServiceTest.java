package com.velsis.speedviolationservice.service;

import com.velsis.speedviolationservice.config.ViolationToleranceProperties;
import com.velsis.speedviolationservice.enums.OriginType;
import com.velsis.speedviolationservice.repository.ViolationRepository;
import com.velsis.speedviolationservice.validation.EvaluationRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationServiceTest {
    private ViolationService violationService;

    @BeforeEach
    void setUp() {
        ViolationRepository repository = new ViolationRepository();
        ViolationEvaluationService evaluationService =
            new ViolationEvaluationService(new ViolationToleranceProperties(7, 7, 100));
        violationService = new ViolationService(evaluationService, repository);
    }

    private EvaluationRequestValidator.ValidatedRequest request(String plate, int measured, int limit) {
        return new EvaluationRequestValidator.ValidatedRequest(
            plate, measured, limit, "RAD-CWB-001", Instant.now(), OriginType.FIXED);
    }

    @Test
    void persistsOnlyWhenThereIsAViolation() {
        var response = violationService.evaluate(request("ABC1D23", 92, 60));

        assertThat(response.hasViolation()).isTrue();
        assertThat(violationService.findByLicensePlate("ABC1D23")).hasSize(1);
    }

    @Test
    void doesNotPersistWhenThereIsNoViolation() {
        var response = violationService.evaluate(request("ABC1D23", 64, 60));

        assertThat(response.hasViolation()).isFalse();
        assertThat(violationService.findByLicensePlate("ABC1D23")).isEmpty();
    }

    @Test
    void returnsEmptyListForUnknownPlate() {
        assertThat(violationService.findByLicensePlate("NEVER999")).isEmpty();
    }
}
