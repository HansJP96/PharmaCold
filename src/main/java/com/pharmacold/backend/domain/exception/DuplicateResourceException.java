package com.pharmacold.backend.domain.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends DomainException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super("%s identified with %s : '%s' already exists".formatted(resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
