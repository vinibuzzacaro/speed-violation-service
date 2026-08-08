package com.velsis.speedviolationservice.exception;

import lombok.Getter;

@Getter
public class RequestValidationException extends RuntimeException {

    private final String errorCode;

    public RequestValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
