package com.pharmacold.backend.application.dto.response;

import com.pharmacold.backend.domain.enums.Role;

import java.util.Set;

public record UserResponse(
        String username,
        String email,
        Set<Role> roles,
        boolean isActive
) {
}
