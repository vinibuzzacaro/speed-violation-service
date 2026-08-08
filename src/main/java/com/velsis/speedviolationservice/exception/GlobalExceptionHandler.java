package com.velsis.speedviolationservice.exception;

import com.velsis.speedviolationservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(RequestValidationException ex) {
        log.warn("Validation error: code={} message={}", ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity.badRequest().body(
            ErrorResponse.of("MISSING_PARAMETER", "Required parameter '" + ex.getParameterName() + "' is missing"));
    }
}
