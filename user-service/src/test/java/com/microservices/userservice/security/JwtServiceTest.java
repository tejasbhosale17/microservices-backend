package com.microservices.userservice.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

  private JwtService jwtService;

  // 256-bit Base64-encoded secret (32 bytes)
  private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
  private static final long EXPIRATION = 86400000L; // 24 hours

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(jwtService, "secret", SECRET);
    ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
  }

  private UserDetails buildUser(String email) {
    return new User(email, "password", Collections.emptyList());
  }

  @Test
  @DisplayName("generateToken - should return non-null token")
  void generateToken_shouldReturnToken() {
    UserDetails userDetails = buildUser("john@example.com");

    String token = jwtService.generateToken(userDetails, "USER");

    assertThat(token).isNotNull().isNotEmpty();
  }

  @Test
  @DisplayName("extractUsername - should return correct subject")
  void extractUsername_shouldReturnEmail() {
    UserDetails userDetails = buildUser("john@example.com");
    String token = jwtService.generateToken(userDetails, "USER");

    String username = jwtService.extractUsername(token);

    assertThat(username).isEqualTo("john@example.com");
  }

  @Test
  @DisplayName("extractClaim - should return role claim")
  void extractClaim_shouldReturnRole() {
    UserDetails userDetails = buildUser("john@example.com");
    String token = jwtService.generateToken(userDetails, "ADMIN");

    String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

    assertThat(role).isEqualTo("ADMIN");
  }

  @Test
  @DisplayName("isTokenValid - should return true for valid token and matching user")
  void isTokenValid_validToken_shouldReturnTrue() {
    UserDetails userDetails = buildUser("john@example.com");
    String token = jwtService.generateToken(userDetails, "USER");

    boolean valid = jwtService.isTokenValid(token, userDetails);

    assertThat(valid).isTrue();
  }

  @Test
  @DisplayName("isTokenValid - should return false when username does not match")
  void isTokenValid_wrongUser_shouldReturnFalse() {
    UserDetails tokenUser = buildUser("john@example.com");
    UserDetails otherUser = buildUser("other@example.com");
    String token = jwtService.generateToken(tokenUser, "USER");

    boolean valid = jwtService.isTokenValid(token, otherUser);

    assertThat(valid).isFalse();
  }

  @Test
  @DisplayName("isTokenValid - should throw ExpiredJwtException for expired token")
  void isTokenValid_expiredToken_shouldThrow() {
    JwtService shortLivedService = new JwtService();
    ReflectionTestUtils.setField(shortLivedService, "secret", SECRET);
    ReflectionTestUtils.setField(shortLivedService, "expiration", -1000L); // already expired

    UserDetails userDetails = buildUser("john@example.com");
    String token = shortLivedService.generateToken(userDetails, "USER");

    // JJWT throws ExpiredJwtException during Claims parsing, before isTokenValid
    // can return false
    assertThatThrownBy(() -> shortLivedService.isTokenValid(token, userDetails))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("extractUsername - should throw for tampered token")
  void extractUsername_tamperedToken_shouldThrow() {
    assertThatThrownBy(() -> jwtService.extractUsername("invalid.token.value"))
        .isInstanceOf(Exception.class);
  }

  @Test
  @DisplayName("generateToken - different roles produce different claims")
  void generateToken_differentRoles_produceDifferentClaims() {
    UserDetails userDetails = buildUser("john@example.com");

    String userToken = jwtService.generateToken(userDetails, "USER");
    String adminToken = jwtService.generateToken(userDetails, "ADMIN");

    String userRole = jwtService.extractClaim(userToken, c -> c.get("role", String.class));
    String adminRole = jwtService.extractClaim(adminToken, c -> c.get("role", String.class));

    assertThat(userRole).isEqualTo("USER");
    assertThat(adminRole).isEqualTo("ADMIN");
  }
}
