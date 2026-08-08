package com.velsis.speedviolationservice.validation;

import com.velsis.speedviolationservice.dto.EvaluateViolationRequest;
import com.velsis.speedviolationservice.enums.OriginType;
import com.velsis.speedviolationservice.exception.RequestValidationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@Component
public class EvaluationRequestValidator {
    private static final String INVALID_PLATE_ERROR = "INVALID_LICENSE_PLATE";
    private static final String INVALID_SPEED_ERROR = "INVALID_MEASURED_SPEED";
    private static final String INVALID_LIMIT_ERROR = "INVALID_SPEED_LIMIT";
    private static final String INVALID_TIMESTAMP_ERROR = "INVALID_CAPTURE_TIMESTAMP";
    private static final String INVALID_ORIGIN_ERROR = "INVALID_ORIGIN";
    private static final String INVALID_EQUIPMENT_ERROR = "INVALID_EQUIPMENT_ID";

    public record ValidatedRequest(
        String licensePlate,
        int measuredSpeed,
        int speedLimit,
        String equipmentId,
        Instant captureTimestamp,
        OriginType origin
    ) {
    }

    public ValidatedRequest validate(String originHeader, EvaluateViolationRequest request) {
        String licensePlate = validateLicensePlate(request.licensePlate());
        int measuredSpeed = validatePositive(request.measuredSpeed(), INVALID_SPEED_ERROR, "measuredSpeed");
        int speedLimit = validatePositive(request.speedLimit(), INVALID_LIMIT_ERROR, "speedLimit");
        String equipmentId = validateEquipmentId(request.equipmentId());
        Instant captureTimestamp = validateCaptureTimestamp(request.captureTimestamp());
        OriginType origin = validateOrigin(originHeader);

        return new ValidatedRequest(licensePlate, measuredSpeed, speedLimit, equipmentId, captureTimestamp, origin);
    }

    private String validateLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new RequestValidationException(INVALID_PLATE_ERROR, "licensePlate is required");
        }
        if (!LicensePlateValidator.isValid(licensePlate)) {
            throw new RequestValidationException(INVALID_PLATE_ERROR, "Invalid license plate format");
        }
        return licensePlate.trim().toUpperCase();
    }

    private int validatePositive(Integer value, String errorCode, String fieldName) {
        if (value == null) {
            throw new RequestValidationException(errorCode, fieldName + " is required");
        }
        if (value <= 0) {
            throw new RequestValidationException(errorCode, fieldName + " must be greater than zero");
        }
        return value;
    }

    private String validateEquipmentId(String equipmentId) {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new RequestValidationException(INVALID_EQUIPMENT_ERROR, "equipmentId is required");
        }
        return equipmentId.trim();
    }

    private Instant validateCaptureTimestamp(String rawTimestamp) {
        if (rawTimestamp == null || rawTimestamp.isBlank()) {
            throw new RequestValidationException(INVALID_TIMESTAMP_ERROR, "captureTimestamp is required");
        }
        Instant captureTimestamp;
        try {
            captureTimestamp = Instant.parse(rawTimestamp);
        } catch (DateTimeParseException e) {
            throw new RequestValidationException(
                INVALID_TIMESTAMP_ERROR, "captureTimestamp must be a valid ISO-8601 instant");
        }
        if (captureTimestamp.isAfter(Instant.now())) {
            throw new RequestValidationException(
                INVALID_TIMESTAMP_ERROR, "captureTimestamp cannot be in the future");
        }
        return captureTimestamp;
    }

    private OriginType validateOrigin(String originHeader) {
        if (originHeader == null || originHeader.isBlank()) {
            throw new RequestValidationException(INVALID_ORIGIN_ERROR, "x-origin header is required");
        }
        boolean matches = Arrays.stream(OriginType.values())
            .anyMatch(value -> value.name().equals(originHeader));
        if (!matches) {
            throw new RequestValidationException(
                INVALID_ORIGIN_ERROR, "x-origin must be one of FIXED, MOBILE, HANDHELD");
        }
        return OriginType.valueOf(originHeader);
    }
}
