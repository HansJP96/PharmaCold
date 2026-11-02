package com.pharmacold.backend.application.dto.request;

import com.pharmacold.backend.application.dto.validator.ValidRoleAllowed;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterUserRequest(

        @Size(
                min = 2,
                max = 50,
                message = "Username must be between 2 and 50 characters long.")
        @NotBlank
        String firstName,

        @Size(
                min = 2,
                max = 50,
                message = "Lastname must be between 2 and 50 characters long.")
        @NotBlank
        String lastName,

        @Email(
                message = "Email must be a valid Gmail",
                regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@gmail\\.+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"
        )
        String email,

        @Pattern(
                message = "Invalid format password. Should contains between 8-64 characters, at least 1 uppercase letter, 1 lowercase letter, 1 number and 1 special character",
                regexp = "^((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{8,64})$"
        )
        String password,

        @ValidRoleAllowed
        List<String> roles
) {
}
