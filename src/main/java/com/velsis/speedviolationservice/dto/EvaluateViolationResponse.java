package com.velsis.speedviolationservice.dto;

import java.time.Instant;

public record EvaluateViolationResponse(
    String licensePlate,
    String equipmentId,
    int measuredSpeed,
    int consideredSpeed,
    int speedLimit,
    double excessPercentage,
    boolean hasViolation,
    ViolationSummary violation,
    Instant processedAt
) {
}
