package com.velsis.speedviolationservice.service;

import com.velsis.speedviolationservice.dto.EvaluateViolationResponse;
import com.velsis.speedviolationservice.dto.EvaluationResult;
import com.velsis.speedviolationservice.dto.ViolationResponse;
import com.velsis.speedviolationservice.dto.ViolationSummary;
import com.velsis.speedviolationservice.model.Violation;
import com.velsis.speedviolationservice.repository.ViolationRepository;
import com.velsis.speedviolationservice.validation.EvaluationRequestValidator.ValidatedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ViolationEvaluationService evaluationService;
    private final ViolationRepository violationRepository;

    public EvaluateViolationResponse evaluate(ValidatedRequest request) {
        EvaluationResult result = evaluationService.evaluate(
            request.measuredSpeed(), request.speedLimit()
        );

        ViolationSummary summary = null;
        if (result.hasViolation()) {
            persistViolation(request, result);
            summary = new ViolationSummary(result.severity(), result.ctbCode());
        }

        return new EvaluateViolationResponse(
            request.licensePlate(),
            request.equipmentId(),
            request.measuredSpeed(),
            result.consideredSpeed(),
            request.speedLimit(),
            result.excessPercentage(),
            result.hasViolation(),
            summary,
            Instant.now()
        );
    }

    public List<ViolationResponse> findByLicensePlate(String licensePlate) {
        return violationRepository.findByLicensePlate(licensePlate).stream()
            .map(this::toResponse)
            .toList();
    }

    private void persistViolation(ValidatedRequest request, EvaluationResult result) {
        Violation violation = new Violation(
            request.licensePlate(),
            request.equipmentId(),
            request.measuredSpeed(),
            result.consideredSpeed(),
            request.speedLimit(),
            result.excessPercentage(),
            result.severity(),
            request.origin(),
            request.captureTimestamp(),
            Instant.now()
        );
        violationRepository.save(violation);
    }

    private ViolationResponse toResponse(Violation violation) {
        return new ViolationResponse(
            violation.licensePlate(),
            violation.equipmentId(),
            violation.measuredSpeed(),
            violation.consideredSpeed(),
            violation.speedLimit(),
            violation.excessPercentage(),
            violation.severity(),
            violation.severity().ctbCode(),
            violation.origin(),
            violation.captureTimestamp(),
            violation.processedAt()
        );
    }
}
