package com.pharmacold.backend.presentation.advice;

import com.pharmacold.backend.domain.exception.InvalidShipmentStateTransitionException;
import com.pharmacold.backend.domain.exception.ResourceNotFoundException;
import com.pharmacold.backend.domain.exception.SensorNotAvailableException;
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

    @ExceptionHandler(SensorNotAvailableException.class)
    public ProblemDetail handleSensorNotAvailable(SensorNotAvailableException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        errorHandler.setTitle("Sensor Conflict");
        errorHandler.setProperty("serialNumber", ex.getSerialNumber());
        errorHandler.setProperty("actualState", ex.getActualState());
        errorHandler.setProperty("timestamp", ex.getTimestamp());
        return errorHandler;
    }

    @ExceptionHandler(InvalidShipmentStateTransitionException.class)
    public ProblemDetail handleInvalidShipmentStateTransition(InvalidShipmentStateTransitionException ex) {
        ProblemDetail errorHandler = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        errorHandler.setTitle("Invalid State Transition");
        errorHandler.setProperty("requiredState", ex.getRequiredState());
        errorHandler.setProperty("actualState", ex.getActualState());
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
}
