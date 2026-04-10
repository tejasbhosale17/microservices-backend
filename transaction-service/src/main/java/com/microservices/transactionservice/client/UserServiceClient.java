package com.microservices.transactionservice.client;

import com.microservices.transactionservice.dto.UserDto;
import com.microservices.transactionservice.exception.ResourceNotFoundException;
import com.microservices.transactionservice.exception.ServiceCommunicationException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://USER-SERVICE")
                .build();
    }

    public UserDto getUserById(Long userId) {
        try {
            return webClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        } catch (Exception e) {
            throw new ServiceCommunicationException("Unable to communicate with User Service");
        }
    }
}
