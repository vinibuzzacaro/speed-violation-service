package com.velsis.speedviolationservice.validation;

import com.velsis.speedviolationservice.dto.EvaluateViolationRequest;
import com.velsis.speedviolationservice.enums.OriginType;
import com.velsis.speedviolationservice.exception.RequestValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvaluationRequestValidatorTest {

    private final EvaluationRequestValidator validator = new EvaluationRequestValidator();

    private EvaluateViolationRequest validRequest() {
        return new EvaluateViolationRequest(
            "ABC1D23", 92, 60, "RAD-CWB-001", Instant.now().minusSeconds(60).toString());
    }

    @Test
    void acceptsAFullyValidRequest() {
        var result = validator.validate("FIXED", validRequest());

        assertThat(result.licensePlate()).isEqualTo("ABC1D23");
        assertThat(result.measuredSpeed()).isEqualTo(92);
        assertThat(result.speedLimit()).isEqualTo(60);
        assertThat(result.equipmentId()).isEqualTo("RAD-CWB-001");
        assertThat(result.origin()).isEqualTo(OriginType.FIXED);
    }

    @Test
    void acceptsOldFormatPlate() {
        var request = new EvaluateViolationRequest(
            "ABC1234", 92, 60, "RAD-CWB-001", Instant.now().minusSeconds(60).toString());
        var result = validator.validate("MOBILE", request);
        assertThat(result.licensePlate()).isEqualTo("ABC1234");
    }

    @Test
    void rejectsMissingLicensePlate() {
        var request = new EvaluateViolationRequest(null, 92, 60, "RAD-CWB-001", Instant.now().toString());
        assertThatThrownBy(() -> validator.validate("FIXED", request))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_LICENSE_PLATE");
    }

    @Test
    void rejectsMalformedLicensePlate() {
        var request = new EvaluateViolationRequest("not-a-plate", 92, 60, "RAD-CWB-001", Instant.now().toString());
        assertThatThrownBy(() -> validator.validate("FIXED", request))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_LICENSE_PLATE");
    }

    @Test
    void rejectsZeroOrNegativeMeasuredSpeed() {
        var request = new EvaluateViolationRequest("ABC1D23", 0, 60, "RAD-CWB-001", Instant.now().toString());
        assertThatThrownBy(() -> validator.validate("FIXED", request))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_MEASURED_SPEED");
    }

    @Test
    void rejectsZeroOrNegativeSpeedLimit() {
        var request = new EvaluateViolationRequest("ABC1D23", 92, -1, "RAD-CWB-001", Instant.now().toString());
        assertThatThrownBy(() -> validator.validate("FIXED", request))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_SPEED_LIMIT");
    }

    @Test
    void rejectsBlankEquipmentId() {
        var request = new EvaluateViolationRequest("ABC1D23", 92, 60, "  ", Instant.now().toString());
        assertThatThrownBy(() -> validator.validate("FIXED", request))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_EQUIPMENT_ID");
    }

    @Test
    void rejectsNonIso8601Timestamp() {
        var request = new EvaluateViolationRequest("ABC1D23", 92, 60, "RAD-CWB-001", "08/06/2026 14:30");
        assertThatThrownBy(() -> validator.validate("FIXED", request))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_CAPTURE_TIMESTAMP");
    }

    @Test
    void rejectsFutureTimestamp() {
        String future = Instant.now().plus(1, ChronoUnit.DAYS).toString();
        var request = new EvaluateViolationRequest("ABC1D23", 92, 60, "RAD-CWB-001", future);
        assertThatThrownBy(() -> validator.validate("FIXED", request))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_CAPTURE_TIMESTAMP");
    }

    @Test
    void rejectsMissingOriginHeader() {
        assertThatThrownBy(() -> validator.validate(null, validRequest()))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_ORIGIN");
    }

    @Test
    void rejectsOriginHeaderWithWrongCase() {
        assertThatThrownBy(() -> validator.validate("fixed", validRequest()))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_ORIGIN");
    }

    @Test
    void rejectsUnknownOriginValue() {
        assertThatThrownBy(() -> validator.validate("SATELLITE", validRequest()))
            .isInstanceOf(RequestValidationException.class)
            .extracting(ex -> ((RequestValidationException) ex).getErrorCode())
            .isEqualTo("INVALID_ORIGIN");
    }
}
