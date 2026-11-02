package com.pharmacold.backend.infrastructure.security.jwt;

import com.pharmacold.backend.infrastructure.security.helpers.AuthorityMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final String secret;
    private final Long ttlMinutes;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${spring.security.secret}") String secret,
            @Value("${spring.security.token-ttl}") Long ttlMinutes,
            @Value("${spring.security.issuer}") String issuer
    ) {
        this.secret = secret;
        this.ttlMinutes = ttlMinutes;
        this.issuer = issuer;
    }

    public String generateToken(String email, Collection<? extends GrantedAuthority> roles) {
        Instant iat = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(this.issuer)
                .subject(email)
                .claim("roles", AuthorityMapper.toBusinessAuthorities(roles))
                .issuedAt(Date.from(iat))
                .expiration(getExpirationTime(iat))
                .signWith(getSecret())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            executeParseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
        } catch (SecurityException | SignatureException e) {
            log.warn("JWT Signature security violation: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token received: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token format: {}", e.getMessage());
        } catch (JwtException e) {
            log.debug("Unexpected JWT exception: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT Token is null or empty: {}", e.getMessage());
        }
        return false;
    }

    public String extractUserIdentifier(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Set<? extends GrantedAuthority> extractUserAuthorities(String token) {
        @SuppressWarnings("unchecked")
        List<String> extractedRoles = extractAllClaims(token).get("roles", List.class);
        return AuthorityMapper.toAuthoritiesWithPrefix(extractedRoles);
    }

    private Jws<Claims> executeParseSignedClaims(String token) {
        return Jwts.parser()
                .requireIssuer(this.issuer)
                .verifyWith(getSecret())
                .build()
                .parseSignedClaims(token);
    }

    private SecretKey getSecret() {
        return Keys.hmacShaKeyFor(this.secret.getBytes(StandardCharsets.UTF_8));
    }

    private Date getExpirationTime(Instant iat) {
        return Date.from(iat.plus(this.ttlMinutes, ChronoUnit.MINUTES));
    }

    private Claims extractAllClaims(String token) {
        return executeParseSignedClaims(token)
                .getPayload();
    }
}
