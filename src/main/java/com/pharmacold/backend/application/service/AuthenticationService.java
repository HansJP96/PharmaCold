package com.pharmacold.backend.application.service;

import com.pharmacold.backend.application.dto.request.LoginRequest;
import com.pharmacold.backend.application.dto.request.RegisterUserRequest;
import com.pharmacold.backend.application.dto.response.AuthenticationResponse;
import com.pharmacold.backend.application.dto.response.UserResponse;
import com.pharmacold.backend.application.mapper.UserMapper;
import com.pharmacold.backend.domain.exception.DuplicateResourceException;
import com.pharmacold.backend.domain.model.User;
import com.pharmacold.backend.domain.repository.UserRepository;
import com.pharmacold.backend.infrastructure.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserMapper userMapper;

    @Transactional
    public UserResponse registerUser(RegisterUserRequest request) {
        boolean userAlreadyExist = userRepository.existsByEmail(request.email());
        if (userAlreadyExist) {
            throw new DuplicateResourceException(User.class.getSimpleName(), "email", request.email());
        }
        User userToRegister = User.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.roles()
        );
        User registeredUser = userRepository.save(userToRegister);
        return userMapper.toResponse(registeredUser);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse authenticateUser(LoginRequest request) {
        Authentication userNotAuthenticated =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication userToAuthenticate = authenticationManager.authenticate(userNotAuthenticated);
        UserDetails userAuthenticated = (UserDetails) userToAuthenticate.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(
                userAuthenticated.getUsername(),
                userAuthenticated.getAuthorities()
        );
        return new AuthenticationResponse(jwt, userAuthenticated.getUsername());
    }
}
