package com.velsis.speedviolationservice.dto;

import java.time.Instant;

public record ErrorResponse(
    String error,
    String message,
    Instant timestamp
) {
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message, Instant.now());
    }
}
