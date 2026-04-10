package com.microservices.transactionservice.exception;

import com.microservices.transactionservice.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    ResourceNotFoundException ex = new ResourceNotFoundException("Transaction not found with id: 1");

    ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Transaction not found with id: 1");
    assertThat(response.getBody().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("handleServiceCommunication - should return 503 with message")
  void handleServiceCommunication_shouldReturn503() {
    ServiceCommunicationException ex = new ServiceCommunicationException("Unable to communicate with User Service");

    ResponseEntity<ErrorResponse> response = handler.handleServiceCommunication(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Unable to communicate with User Service");
    assertThat(response.getBody().getStatus()).isEqualTo(503);
  }

  @Test
  @DisplayName("handleValidationErrors - should return 400 with field errors")
  void handleValidationErrors_shouldReturn400WithDetails() {
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("obj", "amount", "Amount must be positive");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
    assertThat(response.getBody().getDetails()).containsEntry("amount", "Amount must be positive");
  }

  @Test
  @DisplayName("handleGeneral - should return 500 for unexpected exception")
  void handleGeneral_shouldReturn500() {
    Exception ex = new RuntimeException("unexpected");

    ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
  }
}
