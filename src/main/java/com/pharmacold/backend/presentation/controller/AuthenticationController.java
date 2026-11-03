package com.pharmacold.backend.presentation.controller;

import com.pharmacold.backend.application.dto.request.LoginRequest;
import com.pharmacold.backend.application.dto.request.RegisterUserRequest;
import com.pharmacold.backend.application.dto.response.AuthenticationResponse;
import com.pharmacold.backend.application.dto.response.UserResponse;
import com.pharmacold.backend.application.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse registeredUser = authenticationService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse authenticatedUser = authenticationService.authenticateUser(request);
        return ResponseEntity.ok(authenticatedUser);
    }
}
