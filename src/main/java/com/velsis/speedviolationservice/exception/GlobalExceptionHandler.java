package com.velsis.speedviolationservice.exception;

import com.velsis.speedviolationservice.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(RequestValidationException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(
            ErrorResponse.of("MISSING_PARAMETER", "Required parameter '" + ex.getParameterName() + "' is missing"));
    }
}
