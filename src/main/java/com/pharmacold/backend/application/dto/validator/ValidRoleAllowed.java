package com.pharmacold.backend.application.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotEmpty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@NotEmpty( message = "At least one role must be specified")
@Constraint(validatedBy = AllowedRoleValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRoleAllowed {

    String message() default "Specified roles aren't allowed";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
