package com.pharmacold.backend.infrastructure.security;

import com.pharmacold.backend.domain.enums.Role;
import com.pharmacold.backend.infrastructure.security.helpers.AuthorityMapper;
import com.pharmacold.backend.infrastructure.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.pharmacold.backend.infrastructure.security.SecurityConstants.ROLE_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

public class JwtTokenProviderTest {

    private final String TEST_SECRET = "testing-secret-for-jwt-token-verification";
    private final Long TEST_TTL_MINUTES = 15L;
    private final String TEST_ISSUER = "PharmaCold";

    private final String email = "test@gmail.com";
    private final Set<String> roles = Set.of(Role.ADMIN, Role.QUALITY_AUDITOR)
            .stream()
            .map(Role::name)
            .collect(Collectors.toSet());
    private final Set<String> securityRoles = roles
            .stream()
            .map(ROLE_PREFIX::concat)
            .collect(Collectors.toSet());
    private final Set<? extends GrantedAuthority> authorities = AuthorityMapper.toAuthoritiesWithPrefix(roles);

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_TTL_MINUTES, TEST_ISSUER);
    }

    @Test
    @DisplayName("Should generate token successfully when the user is authenticated")
    void shouldGenerateTokenSuccessfully_whenUserIsAuthenticated() {
        //ARRANGE //ACT
        String token = jwtTokenProvider.generateToken(email, authorities);

        //ASSERT
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.extractUserIdentifier(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.extractUserAuthorities(token))
                .extracting(GrantedAuthority::getAuthority)
                .containsAll(securityRoles);
    }

    @Test
    @DisplayName("Should return false when generated token was modified")
    void shouldReturnFalse_whenGeneratedTokenWasModified() {
        //ARRANGE
        String token = jwtTokenProvider.generateToken(email, authorities);

        //ACT
        token = token.substring(0, token.length() - 1).concat("XXXX");

        //ASSERT
        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Should return false when token is expired")
    void shouldReturnFalse_whenTokenIsExpired() {
        //ARRANGE
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider(TEST_SECRET, -1L, TEST_ISSUER);

        //ACT
        String token = expiredTokenProvider.generateToken(email, authorities);

        //ASSERT
        assertThat(expiredTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Should return false when token issuer is not correct")
    void shouldReturnFalse_whenTokenIssuerIsNotCorrect() {
        //ARRANGE
        JwtTokenProvider fakeIssuerTokenProvider = new JwtTokenProvider(TEST_SECRET, TEST_TTL_MINUTES, "fake");

        //ACT
        String token = jwtTokenProvider.generateToken(email, authorities);

        //ASSERT
        assertThat(fakeIssuerTokenProvider.validateToken(token)).isFalse();
    }
}
