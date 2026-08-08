package com.velsis.speedviolationservice.dto;

public record EvaluateViolationRequest(
    String licensePlate,
    Integer measuredSpeed,
    Integer speedLimit,
    String equipmentId,
    String captureTimestamp
) {
}
