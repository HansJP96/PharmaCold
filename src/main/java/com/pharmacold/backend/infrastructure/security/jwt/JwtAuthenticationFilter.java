package com.pharmacold.backend.infrastructure.security.jwt;

import com.pharmacold.backend.infrastructure.security.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(SecurityConstants.AUTH_HEADER);
        if (requireAuthentication(authorizationHeader)) {
            String token = authorizationHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.extractUserIdentifier(token);
                Set<? extends GrantedAuthority> authorities = jwtTokenProvider.extractUserAuthorities(token);
                UsernamePasswordAuthenticationToken userAuth
                        = new UsernamePasswordAuthenticationToken(email, null, authorities);
                userAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(userAuth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean requireAuthentication(String authorizationHeader) {
        return SecurityContextHolder.getContext().getAuthentication() == null &&
                authorizationHeader != null &&
                authorizationHeader.startsWith(SecurityConstants.BEARER_PREFIX);
    }
}
