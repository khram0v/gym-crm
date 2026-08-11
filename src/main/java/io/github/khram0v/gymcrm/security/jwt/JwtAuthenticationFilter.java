package io.github.khram0v.gymcrm.security.jwt;

import io.github.khram0v.gymcrm.security.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(token);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                return;
            }

            String username = jwtService.extractUsername(token);
            if (!jwtService.isValid(token, username)) {
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!userDetails.isEnabled()) {
                return;
            }

            var authentication = UsernamePasswordAuthenticationToken.authenticated(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            // leave SecurityContext unauthenticated - request will be rejected
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
