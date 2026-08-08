package com.velsis.speedviolationservice.dto;

import com.velsis.speedviolationservice.enums.OriginType;

import java.time.Instant;

public record ValidatedRequest(
    String licensePlate,
    int measuredSpeed,
    int speedLimit,
    String equipmentId,
    Instant captureTimestamp,
    OriginType origin
) {
}
