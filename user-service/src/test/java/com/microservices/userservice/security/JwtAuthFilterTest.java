package com.microservices.userservice.security;

import com.microservices.userservice.security.JwtAuthFilter;
import com.microservices.userservice.security.JwtService;
import com.microservices.userservice.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthFilter jwtAuthFilter;

  @BeforeEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("doFilterInternal - no Authorization header should pass through without authentication")
  void doFilterInternal_noAuthHeader_shouldContinueChain() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal - Authorization header without Bearer prefix should pass through")
  void doFilterInternal_authHeaderWithoutBearer_shouldContinueChain() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Basic abc123");

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal - valid token should set authentication in SecurityContext")
  void doFilterInternal_validToken_shouldSetAuthentication() throws Exception {
    String token = "valid.jwt.token";
    UserDetails userDetails = new User("john@example.com", "password", Collections.emptyList());

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn("john@example.com");
    when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
        .isEqualTo("john@example.com");
  }

  @Test
  @DisplayName("doFilterInternal - invalid token should not set authentication")
  void doFilterInternal_invalidToken_shouldNotSetAuthentication() throws Exception {
    String token = "invalid.jwt.token";
    UserDetails userDetails = new User("john@example.com", "password", Collections.emptyList());

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn("john@example.com");
    when(userDetailsService.loadUserByUsername("john@example.com")).thenReturn(userDetails);
    when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal - null username from token should not attempt loading user")
  void doFilterInternal_nullUsername_shouldNotLoadUser() throws Exception {
    String token = "some.jwt.token";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractUsername(token)).thenReturn(null);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);
  }
}
