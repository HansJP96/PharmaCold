package com.pharmacold.backend.infrastructure.security;

import com.pharmacold.backend.domain.enums.Role;
import com.pharmacold.backend.domain.model.User;
import com.pharmacold.backend.infrastructure.security.helpers.AuthorityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserDetailsAdapter implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityMapper.toAuthoritiesWithPrefix(
                user.getRoles().stream()
                        .map(Role::name)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return this.user.isActive();
    }
}
