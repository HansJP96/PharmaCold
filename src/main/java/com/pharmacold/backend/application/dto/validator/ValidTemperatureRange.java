package com.pharmacold.backend.application.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TemperatureRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTemperatureRange {

    String message() default "The minimum temperature must be strictly lower than the maximum temperature";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
