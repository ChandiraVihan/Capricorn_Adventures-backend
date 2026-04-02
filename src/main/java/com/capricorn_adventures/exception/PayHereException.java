package com.capricorn_adventures.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class PayHereException extends RuntimeException {
    public PayHereException(String message) {
        super(message);
    }
}
