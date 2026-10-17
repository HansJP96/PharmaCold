package com.pharmacold.backend.presentation.advice;

import com.pharmacold.backend.domain.exception.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        errorHandler.setTitle("Resource Not Found");
        errorHandler.setProperty("resourceName", ex.getResourceName());
        errorHandler.setProperty("fieldValue", ex.getFieldValue());
        errorHandler.setProperty("fieldName", ex.getFieldName());
        errorHandler.setProperty("timestamp", ex.getTimestamp());
        return errorHandler;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicatedResourceException(DuplicateResourceException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        errorHandler.setTitle("Resource Duplicated");
        errorHandler.setProperty("resourceName", ex.getResourceName());
        errorHandler.setProperty("fieldName", ex.getFieldName());
        errorHandler.setProperty("fieldValue", ex.getFieldValue());
        errorHandler.setProperty("timestamp", ex.getTimestamp());
        return errorHandler;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        errorHandler.setTitle("Fields Validation Error");
        Map<String, Object> invalidFields = ex.getBindingResult().getFieldErrors().stream()
                .collect(
                        Collectors.toMap(
                                FieldError::getField,
                                fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid Field Value",
                                (existingValue, newValue) -> existingValue
                        )
                );
        errorHandler.setProperty("invalidFields", invalidFields);
        return errorHandler;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        errorHandler.setTitle("Unexpected Error");
        errorHandler.setProperty("timestamp", Instant.now());
        return errorHandler;
    }

    //SENSOR
    @ExceptionHandler(InvalidSensorStateException.class)
    public ProblemDetail handleInvalidSensorStateException(InvalidSensorStateException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        errorHandler.setTitle("Invalid State to handle the sensor");
        errorHandler.setProperty("serialNumber", ex.getSerialNumber());
        errorHandler.setProperty("currentState", ex.getCurrentState());
        errorHandler.setProperty("timestamp", ex.getTimestamp());
        return errorHandler;
    }

    @ExceptionHandler(InconsistentTelemetrySensorException.class)
    public ProblemDetail handleInconsistentTelemetrySensorException(InconsistentTelemetrySensorException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        errorHandler.setTitle("Inconsistency between sensor for telemetry and shipment");
        errorHandler.setProperty("associatedSensor", ex.getTelemetrySensor());
        errorHandler.setProperty("shipmentAssignedSensor", ex.getShipmentAssignedSensor());
        errorHandler.setProperty("timestamp", ex.getTimestamp());
        return errorHandler;
    }

    //SHIPMENT
    @ExceptionHandler(InvalidShipmentStateTransitionException.class)
    public ProblemDetail handleInvalidShipmentStateTransition(InvalidShipmentStateTransitionException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        errorHandler.setTitle("Invalid State Transition");
        errorHandler.setProperty("targetState", ex.getTargetState());
        errorHandler.setProperty("currentState", ex.getCurrentState());
        errorHandler.setProperty("timestamp", ex.getTimestamp());
        return errorHandler;
    }

    @ExceptionHandler(InvalidShipmentStateException.class)
    public ProblemDetail handleInvalidShipmentStateException(InvalidShipmentStateException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        errorHandler.setTitle("Invalid State to handle the shipment");
        errorHandler.setProperty("trackingNumber", ex.getTrackingNumber());
        errorHandler.setProperty("currentState", ex.getCurrentState());
        errorHandler.setProperty("timestamp", ex.getTimestamp());
        return errorHandler;
    }
}
