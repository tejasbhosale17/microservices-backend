package com.microservices.transactionservice.service;

import com.microservices.transactionservice.client.UserServiceClient;
import com.microservices.transactionservice.dto.CreateTransactionRequest;
import com.microservices.transactionservice.dto.TransactionResponse;
import com.microservices.transactionservice.dto.UpdateStatusRequest;
import com.microservices.transactionservice.dto.UserDto;
import com.microservices.transactionservice.entity.Transaction;
import com.microservices.transactionservice.entity.TransactionStatus;
import com.microservices.transactionservice.entity.TransactionType;
import com.microservices.transactionservice.exception.ResourceNotFoundException;
import com.microservices.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
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
    @DisplayName("Should create transaction successfully")
    void createTransaction_shouldReturnResponse() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .userId(1L)
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.CREDIT)
                .description("Test transaction")
                .build();

        UserDto userDto = UserDto.builder().id(1L).name("John").email("john@example.com").build();
        when(userServiceClient.getUserById(1L)).thenReturn(userDto);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionResponse response = transactionService.createTransaction(request);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.PENDING);
        verify(userServiceClient, times(1)).getUserById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should get transaction by id")
    void getTransactionById_shouldReturnResponse() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        TransactionResponse response = transactionService.getTransactionById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDescription()).isEqualTo("Test transaction");
    }

    @Test
    @DisplayName("Should throw when transaction not found")
    void getTransactionById_notFound_shouldThrow() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Transaction not found with id: 99");
    }

    @Test
    @DisplayName("Should get transactions by user id")
    void getTransactionsByUserId_shouldReturnList() {
        Transaction tx2 = Transaction.builder()
                .id(2L).userId(1L).amount(new BigDecimal("200.00"))
                .type(TransactionType.DEBIT).description("Second transaction")
                .status(TransactionStatus.COMPLETED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(testTransaction, tx2));

        List<TransactionResponse> responses = transactionService.getTransactionsByUserId(1L);

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("Should update transaction status")
    void updateTransactionStatus_shouldReturnUpdated() {
        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .status(TransactionStatus.COMPLETED)
                .build();

        testTransaction.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionResponse response = transactionService.updateTransactionStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should return all transactions")
    void getAllTransactions_shouldReturnList() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList(testTransaction));

        List<TransactionResponse> responses = transactionService.getAllTransactions();

        assertThat(responses).hasSize(1);
    }
}
