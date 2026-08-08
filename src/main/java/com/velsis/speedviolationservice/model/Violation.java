package com.velsis.speedviolationservice.model;

import com.velsis.speedviolationservice.enums.OriginType;
import com.velsis.speedviolationservice.enums.ViolationSeverity;

import java.time.Instant;

public record Violation(
    String licensePlate,
    String equipmentId,
    int measuredSpeed,
    int consideredSpeed,
    int speedLimit,
    double excessPercentage,
    ViolationSeverity severity,
    OriginType origin,
    Instant captureTimestamp,
    Instant processedAt
) {
}

