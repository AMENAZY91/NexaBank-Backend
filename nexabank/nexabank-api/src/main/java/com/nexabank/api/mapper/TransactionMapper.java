package com.nexabank.api.mapper;

import com.nexabank.api.dto.transaction.CreateTransactionRequest;
import com.nexabank.api.dto.transaction.TransactionResponse;
import com.nexabank.api.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper para transformaciones Transaction ↔ DTO.
 */
@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccountId())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .timestamp(transaction.getTimestamp())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    public Transaction fromCreateRequest(CreateTransactionRequest request, String userId) {
        Instant now = Instant.now();
        return Transaction.builder()
                .accountId(request.getAccountId())
                .userId(userId)
                .type(request.getType())
                .category(request.getCategory())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .status("PENDING")
                .timestamp(now)
                .createdAt(now)
                .build();
    }
}
