package com.pharmacold.backend.infrastructure.security.helpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pharmacold.backend.infrastructure.security.SecurityConstants.ROLE_PREFIX;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorityMapper {

    public static Set<? extends GrantedAuthority> toAuthoritiesWithPrefix(Collection<String> roleCollection) {
        return roleCollection.stream()
                .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX.concat(role))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public static Set<String> toBusinessAuthorities(Collection<? extends GrantedAuthority> roleCollection) {
        return roleCollection.stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith(ROLE_PREFIX) ? role.substring(ROLE_PREFIX.length()) : role)
                .collect(Collectors.toSet());
    }
}
