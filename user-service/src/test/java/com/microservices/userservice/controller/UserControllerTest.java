package com.microservices.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.userservice.dto.UpdateUserRequest;
import com.microservices.userservice.dto.UserResponse;
import com.microservices.userservice.entity.Role;
import com.microservices.userservice.exception.ResourceNotFoundException;
import com.microservices.userservice.security.JwtAuthFilter;
import com.microservices.userservice.security.JwtService;
import com.microservices.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private UserResponse createUserResponse() {
        return UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/users/1 - should return user")
    void getUserById_shouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(createUserResponse());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /api/users/99 - should return 404")
    void getUserById_notFound_shouldReturn404() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users - should return list")
    void getAllUsers_shouldReturnList() throws Exception {
        UserResponse user1 = createUserResponse();
        UserResponse user2 = UserResponse.builder()
                .id(2L).name("Jane Doe").email("jane@example.com").role(Role.ADMIN)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("PUT /api/users/1 - should update user")
    void updateUser_shouldReturn200() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder().name("Updated Name").build();
        UserResponse response = createUserResponse();
        response.setName("Updated Name");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("DELETE /api/users/1 - should return 204")
    void deleteUser_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
