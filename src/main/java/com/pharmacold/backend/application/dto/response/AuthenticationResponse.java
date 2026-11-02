package com.pharmacold.backend.application.dto.response;

public record AuthenticationResponse(
        String token,
        String username
) {
}
