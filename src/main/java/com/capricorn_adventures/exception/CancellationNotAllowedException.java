package com.capricorn_adventures.exception;

public class CancellationNotAllowedException extends RuntimeException {
    public CancellationNotAllowedException(String message) {
        super(message);
    }
}
