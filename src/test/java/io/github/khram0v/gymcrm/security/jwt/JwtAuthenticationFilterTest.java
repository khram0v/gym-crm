package io.github.khram0v.gymcrm.security.jwt;

import io.github.khram0v.gymcrm.security.CustomUserDetailsService;
import io.github.khram0v.gymcrm.security.Role;
import io.github.khram0v.gymcrm.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_whenNoAuthorizationHeader_doesNotAuthenticate_andContinuesChain() throws Exception {
        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, tokenBlacklistService, userDetailsService);
    }

    @Test
    void doFilter_whenHeaderNotBearer_doesNotAuthenticate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc123");

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, tokenBlacklistService, userDetailsService);
    }

    @Test
    void doFilter_whenTokenBlacklisted_doesNotAuthenticate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token123");
        when(tokenBlacklistService.isBlacklisted("token123")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilter_whenTokenInvalid_doesNotAuthenticate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token123");
        when(tokenBlacklistService.isBlacklisted("token123")).thenReturn(false);
        when(jwtService.extractUsername("token123")).thenReturn("John.Doe");
        when(jwtService.isValid("token123", "John.Doe")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilter_whenUserNotFound_doesNotAuthenticate_andDoesNotPropagate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token123");
        when(tokenBlacklistService.isBlacklisted("token123")).thenReturn(false);
        when(jwtService.extractUsername("token123")).thenReturn("Ghost");
        when(jwtService.isValid("token123", "Ghost")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("Ghost"))
                .thenThrow(new UsernameNotFoundException("User not found: Ghost"));

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenUserDisabled_doesNotAuthenticate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token123");
        UserPrincipal disabledPrincipal = new UserPrincipal("John.Doe", "encodedPass", false, Role.TRAINEE);
        when(tokenBlacklistService.isBlacklisted("token123")).thenReturn(false);
        when(jwtService.extractUsername("token123")).thenReturn("John.Doe");
        when(jwtService.isValid("token123", "John.Doe")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("John.Doe")).thenReturn(disabledPrincipal);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenTokenValid_setsAuthenticationWithPrincipalAndAuthorities() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token123");
        UserPrincipal principal = new UserPrincipal("John.Doe", "encodedPass", true, Role.TRAINEE);
        when(tokenBlacklistService.isBlacklisted("token123")).thenReturn(false);
        when(jwtService.extractUsername("token123")).thenReturn("John.Doe");
        when(jwtService.isValid("token123", "John.Doe")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("John.Doe")).thenReturn(principal);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isSameAs(principal);
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TRAINEE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_whenAlreadyAuthenticated_doesNotReAuthenticate() throws Exception {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token123");
        Authentication existing = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existing);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, tokenBlacklistService, userDetailsService);
    }
}
