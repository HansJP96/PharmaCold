package com.pharmacold.backend.application.dto.validator;

import com.pharmacold.backend.application.dto.request.CreateShipmentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.compare.ComparableUtils;

public class TemperatureRangeValidator implements ConstraintValidator<ValidTemperatureRange, CreateShipmentRequest> {


    @Override
    public boolean isValid(CreateShipmentRequest request, ConstraintValidatorContext context) {
        if (request.minTempCelsius() == null || request.maxTempCelsius() == null) return true;
        return ComparableUtils.is(request.minTempCelsius()).lessThan(request.maxTempCelsius());
    }
}
