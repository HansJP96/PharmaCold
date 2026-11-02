package com.pharmacold.backend.domain.model;

import com.pharmacold.backend.domain.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(access = AccessLevel.PACKAGE)
@Entity
@Table(name = "users")
public class User extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email(message = "Email must be a valid Gmail", regexp = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@gmail\\.+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
    @Column(unique = true, nullable = false)
    private String email;

    @Size(max = 255)
    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", nullable = false)
    )
    @Enumerated(value = EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 50)
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    public static User create(String firstName, String lastName, String email, String passwordHash, List<String> roles) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .passwordHash(passwordHash)
                .roles(
                        roles.stream()
                                .map(Role::valueOf)
                                .collect(Collectors.toSet())
                )
                .build();
    }
}
