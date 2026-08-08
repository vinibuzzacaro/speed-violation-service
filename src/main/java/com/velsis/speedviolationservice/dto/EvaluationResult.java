package com.velsis.speedviolationservice.dto;

import com.velsis.speedviolationservice.enums.ViolationSeverity;

public record EvaluationResult(
    int consideredSpeed,
    double excessPercentage,
    boolean hasViolation,
    ViolationSeverity severity,
    String ctbCode
) {
}
