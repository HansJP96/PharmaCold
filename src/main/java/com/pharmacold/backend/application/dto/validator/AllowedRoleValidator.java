package com.pharmacold.backend.application.dto.validator;

import com.pharmacold.backend.domain.enums.Role;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AllowedRoleValidator implements ConstraintValidator<ValidRoleAllowed, List<String>> {

    private static final Set<String> ALLOWED_ROLES =
            Arrays.stream(Role.values())
                    .map(Role::name)
                    .collect(Collectors.toSet());

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return false;
        Set<String> requestedRoles = new HashSet<>(value);
        return ALLOWED_ROLES.containsAll(requestedRoles);
    }
}
