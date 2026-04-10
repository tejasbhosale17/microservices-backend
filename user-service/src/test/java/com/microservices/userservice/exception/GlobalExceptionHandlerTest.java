package com.microservices.userservice.exception;

import com.microservices.userservice.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  @DisplayName("handleResourceNotFound - should return 404 with message")
  void handleResourceNotFound_shouldReturn404() {
    ResourceNotFoundException ex = new ResourceNotFoundException("User not found with id: 1");

    ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("User not found with id: 1");
    assertThat(response.getBody().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("handleDuplicateResource - should return 409 with message")
  void handleDuplicateResource_shouldReturn409() {
    DuplicateResourceException ex = new DuplicateResourceException("Email already registered");

    ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Email already registered");
    assertThat(response.getBody().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("handleBadCredentials - should return 401 with fixed message")
  void handleBadCredentials_shouldReturn401() {
    BadCredentialsException ex = new BadCredentialsException("wrong");

    ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
  }

  @Test
  @DisplayName("handleAccessDenied - should return 403")
  void handleAccessDenied_shouldReturn403() {
    AccessDeniedException ex = new AccessDeniedException("denied");

    ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
  }

  @Test
  @DisplayName("handleValidationErrors - should return 400 with field errors map")
  void handleValidationErrors_shouldReturn400WithDetails() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("obj", "email", "Email must be valid");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
    assertThat(response.getBody().getDetails()).containsEntry("email", "Email must be valid");
  }

  @Test
  @DisplayName("handleGeneral - should return 500 for unexpected exception")
  void handleGeneral_shouldReturn500() {
    Exception ex = new RuntimeException("something went wrong");

    ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
  }
}
