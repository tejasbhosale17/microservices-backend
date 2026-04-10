package com.microservices.userservice.security;

import com.microservices.userservice.entity.Role;
import com.microservices.userservice.entity.User;
import com.microservices.userservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  @Test
  @DisplayName("loadUserByUsername - should return UserDetails for existing user")
  void loadUserByUsername_existingUser_shouldReturnUserDetails() {
    User user = User.builder()
        .id(1L)
        .email("john@example.com")
        .password("encoded_password")
        .role(Role.USER)
        .build();
    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

    UserDetails result = customUserDetailsService.loadUserByUsername("john@example.com");

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("john@example.com");
    assertThat(result.getPassword()).isEqualTo("encoded_password");
    assertThat(result.getAuthorities()).hasSize(1);
    assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
  }

  @Test
  @DisplayName("loadUserByUsername - should return ADMIN authority for ADMIN role")
  void loadUserByUsername_adminUser_shouldReturnAdminAuthority() {
    User user = User.builder()
        .id(2L)
        .email("admin@example.com")
        .password("encoded_password")
        .role(Role.ADMIN)
        .build();
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

    UserDetails result = customUserDetailsService.loadUserByUsername("admin@example.com");

    assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
  }

  @Test
  @DisplayName("loadUserByUsername - should throw UsernameNotFoundException for unknown email")
  void loadUserByUsername_unknownEmail_shouldThrow() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown@example.com"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("unknown@example.com");
  }
}
