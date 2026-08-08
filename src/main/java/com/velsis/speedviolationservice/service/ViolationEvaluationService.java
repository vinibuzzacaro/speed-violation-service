package com.velsis.speedviolationservice.service;

import com.velsis.speedviolationservice.config.ViolationToleranceProperties;
import com.velsis.speedviolationservice.dto.EvaluationResult;
import com.velsis.speedviolationservice.enums.ViolationSeverity;
import org.springframework.stereotype.Service;

@Service
public class ViolationEvaluationService {

    private final ViolationToleranceProperties tolerance;

    public ViolationEvaluationService(ViolationToleranceProperties tolerance) {
        this.tolerance = tolerance;
    }

    public int calculateConsideredSpeed(int measuredSpeed, int speedLimit) {
        if (speedLimit <= tolerance.percentageThreshold()) {
            return measuredSpeed - tolerance.toleranceMarginKmh();
        }

        return (measuredSpeed * (100 - tolerance.toleranceMarginPercentage())) / 100;
    }

    public EvaluationResult evaluate(int measuredSpeed, int speedLimit) {
        int consideredSpeed = calculateConsideredSpeed(measuredSpeed, speedLimit);

        if (consideredSpeed <= speedLimit) {
            return new EvaluationResult(consideredSpeed, 0.0, false, null, null);
        }

        double excessPercentage = roundToTwoDecimals(100.0 * (consideredSpeed - speedLimit) / speedLimit);
        ViolationSeverity severity = ViolationSeverity.fromExcessPercentage(excessPercentage);

        return new EvaluationResult(consideredSpeed, excessPercentage, true, severity, severity.ctbCode());
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
