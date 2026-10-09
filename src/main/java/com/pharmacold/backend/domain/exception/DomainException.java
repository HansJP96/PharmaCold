package com.pharmacold.backend.domain.exception;

import lombok.Getter;

import java.time.Instant;

public abstract class DomainException extends RuntimeException {

    @Getter
    protected Instant timestamp;

    public DomainException(String message) {
        super(message);
        this.timestamp = Instant.now();
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = Instant.now();
    }
}
