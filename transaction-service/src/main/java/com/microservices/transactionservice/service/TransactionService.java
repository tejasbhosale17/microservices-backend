package com.microservices.transactionservice.service;

import com.microservices.transactionservice.client.UserServiceClient;
import com.microservices.transactionservice.dto.CreateTransactionRequest;
import com.microservices.transactionservice.dto.TransactionResponse;
import com.microservices.transactionservice.dto.UpdateStatusRequest;
import com.microservices.transactionservice.entity.Transaction;
import com.microservices.transactionservice.entity.TransactionStatus;
import com.microservices.transactionservice.exception.ResourceNotFoundException;
import com.microservices.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserServiceClient userServiceClient;

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        // Validate user exists via User Service
        userServiceClient.getUserById(request.getUserId());

        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .status(TransactionStatus.PENDING)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse updateTransactionStatus(Long id, UpdateStatusRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        transaction.setStatus(request.getStatus());
        Transaction updated = transactionRepository.save(transaction);
        return mapToResponse(updated);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
