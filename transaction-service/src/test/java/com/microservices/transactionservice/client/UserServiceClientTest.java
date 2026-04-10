package com.microservices.transactionservice.client;

import com.microservices.transactionservice.dto.UserDto;
import com.microservices.transactionservice.exception.ResourceNotFoundException;
import com.microservices.transactionservice.exception.ServiceCommunicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

  private WebClient.Builder webClientBuilder;
  private WebClient webClient;
  private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
  private WebClient.RequestHeadersSpec requestHeadersSpec;
  private WebClient.ResponseSpec responseSpec;

  private UserServiceClient userServiceClient;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    webClientBuilder = mock(WebClient.Builder.class);
    webClient = mock(WebClient.class);
    requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
    requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
    responseSpec = mock(WebClient.ResponseSpec.class);

    when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
    when(webClientBuilder.build()).thenReturn(webClient);

    userServiceClient = new UserServiceClient(webClientBuilder);
  }

  @Test
  @DisplayName("getUserById - should return UserDto on success")
  @SuppressWarnings("unchecked")
  void getUserById_success_shouldReturnUserDto() {
    UserDto expected = UserDto.builder()
        .id(1L)
        .name("John Doe")
        .email("john@example.com")
        .build();

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(UserDto.class)).thenReturn(Mono.just(expected));

    UserDto result = userServiceClient.getUserById(1L);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("John Doe");
  }

  @Test
  @DisplayName("getUserById - should throw ResourceNotFoundException on 404")
  @SuppressWarnings("unchecked")
  void getUserById_notFound_shouldThrowResourceNotFoundException() {
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(UserDto.class))
        .thenReturn(Mono.error(WebClientResponseException.NotFound
            .create(404, "Not Found", null, null, null)));

    assertThatThrownBy(() -> userServiceClient.getUserById(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("User not found with id: 99");
  }

  @Test
  @DisplayName("getUserById - should throw ServiceCommunicationException on generic error")
  @SuppressWarnings("unchecked")
  void getUserById_genericError_shouldThrowServiceCommunicationException() {
    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(UserDto.class))
        .thenReturn(Mono.error(new RuntimeException("connection refused")));

    assertThatThrownBy(() -> userServiceClient.getUserById(1L))
        .isInstanceOf(ServiceCommunicationException.class)
        .hasMessageContaining("Unable to communicate with User Service");
  }
}
