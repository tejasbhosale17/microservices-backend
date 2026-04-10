package com.microservices.transactionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.transactionservice.dto.CreateTransactionRequest;
import com.microservices.transactionservice.dto.TransactionResponse;
import com.microservices.transactionservice.dto.UpdateStatusRequest;
import com.microservices.transactionservice.entity.TransactionStatus;
import com.microservices.transactionservice.entity.TransactionType;
import com.microservices.transactionservice.exception.ResourceNotFoundException;
import com.microservices.transactionservice.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionResponse createResponse() {
        return TransactionResponse.builder()
                .id(1L)
                .userId(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.CREDIT)
                .description("Test transaction")
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/transactions - should return 201")
    void createTransaction_shouldReturn201() throws Exception {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.CREDIT)
                .description("Test transaction")
                .build();

        when(transactionService.createTransaction(any(CreateTransactionRequest.class)))
                .thenReturn(createResponse());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("CREDIT"));
    }

    @Test
    @DisplayName("POST /api/transactions - should return 400 for invalid input")
    void createTransaction_invalidInput_shouldReturn400() throws Exception {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(null)
                .amount(null)
                .description("")
                .build();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/transactions/1 - should return transaction")
    void getTransactionById_shouldReturn200() throws Exception {
        when(transactionService.getTransactionById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test transaction"));
    }

    @Test
    @DisplayName("GET /api/transactions/99 - should return 404")
    void getTransactionById_notFound_shouldReturn404() throws Exception {
        when(transactionService.getTransactionById(99L))
                .thenThrow(new ResourceNotFoundException("Transaction not found with id: 99"));

        mockMvc.perform(get("/api/transactions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/transactions/user/1 - should return list")
    void getTransactionsByUserId_shouldReturn200() throws Exception {
        when(transactionService.getTransactionsByUserId(1L))
                .thenReturn(Arrays.asList(createResponse()));

        mockMvc.perform(get("/api/transactions/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/transactions - should return all")
    void getAllTransactions_shouldReturn200() throws Exception {
        when(transactionService.getAllTransactions())
                .thenReturn(Arrays.asList(createResponse()));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /api/transactions/1/status - should update status")
    void updateStatus_shouldReturn200() throws Exception {
        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status(TransactionStatus.COMPLETED)
                .build();

        TransactionResponse response = createResponse();
        response.setStatus(TransactionStatus.COMPLETED);

        when(transactionService.updateTransactionStatus(eq(1L), any(UpdateStatusRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/transactions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
