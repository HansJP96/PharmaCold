package com.pharmacold.backend.domain.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends DomainException {

    private final String resourceName;
    private final String fieldIdName;
    private final Object fieldIdValue;

    public DuplicateResourceException(String resourceName, String fieldIdName, Object fieldIdValue) {
        super("%s identified with %s : '%s' already exists".formatted(resourceName, fieldIdName, fieldIdValue));
        this.resourceName = resourceName;
        this.fieldIdName = fieldIdName;
        this.fieldIdValue = fieldIdValue;
    }
}
