package com.nexabank.api.service;

import com.nexabank.api.dto.transaction.CreateTransactionRequest;
import com.nexabank.api.dto.transaction.TransactionResponse;
import com.nexabank.api.exception.InsufficientFundsException;
import com.nexabank.api.exception.InvalidTransactionException;
import com.nexabank.api.exception.ResourceNotFoundException;
import com.nexabank.api.mapper.TransactionMapper;
import com.nexabank.api.model.Account;
import com.nexabank.api.model.AuditLog;
import com.nexabank.api.model.Transaction;
import com.nexabank.api.repository.AccountRepository;
import com.nexabank.api.repository.AuditLogRepository;
import com.nexabank.api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              AuditLogRepository auditLogRepository,
                              TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.auditLogRepository = auditLogRepository;
        this.transactionMapper = transactionMapper;
    }

    public List<TransactionResponse> getTransactions(String accountId, String userId) {
        if (accountId != null && !accountId.isBlank()) {
            return transactionRepository.findByAccountId(accountId).stream()
                    .map(transactionMapper::toResponse)
                    .toList();
        }
        if (userId != null && !userId.isBlank()) {
            return transactionRepository.findByUserId(userId).stream()
                    .map(transactionMapper::toResponse)
                    .toList();
        }
        return List.of();
    }

    public TransactionResponse getTransactionById(String id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
        return transactionMapper.toResponse(tx);
    }

    public List<Transaction> getAllTransactionEntities(String accountId, String userId) {
        if (accountId != null && !accountId.isBlank()) {
            return transactionRepository.findByAccountId(accountId);
        }
        if (userId != null && !userId.isBlank()) {
            return transactionRepository.findByUserId(userId);
        }
        return List.of();
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request, String userId) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountId()));

        if (!account.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new InvalidTransactionException("Currency mismatch: account=" + account.getCurrency() + ", request=" + request.getCurrency());
        }

        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance;

        String type = request.getType().toUpperCase();
        if ("EXPENSE".equals(type)) {
            if (currentBalance.compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException("Insufficient funds: current balance " + currentBalance + " is less than " + request.getAmount());
            }
            newBalance = currentBalance.subtract(request.getAmount());
        } else if ("INCOME".equals(type)) {
            newBalance = currentBalance.add(request.getAmount());
        } else {
            throw new InvalidTransactionException("Unsupported transaction type: " + type);
        }

        // 1. Guardar transacción con estado COMPLETED
        Transaction tx = transactionMapper.fromCreateRequest(request, userId != null ? userId : account.getUserId());
        tx.setStatus("COMPLETED");
        Transaction savedTx = transactionRepository.save(tx);

        // 2. Actualizar saldo de la cuenta
        accountRepository.updateBalance(account.getId(), newBalance);

        // 3. Registrar auditoría
        auditLogRepository.save(AuditLog.builder()
                .userId(userId != null ? userId : account.getUserId())
                .action("CREATE_TRANSACTION")
                .resourceType("TRANSACTION")
                .resourceId(savedTx.getId())
                .status("SUCCESS")
                .timestamp(Instant.now())
                .metadata(Map.of(
                        "accountId", account.getId(),
                        "amount", request.getAmount().doubleValue(),
                        "type", type,
                        "previousBalance", currentBalance.doubleValue(),
                        "newBalance", newBalance.doubleValue()
                ))
                .build());

        log.info("Processed {} of {} on account {}. New balance: {}", type, request.getAmount(), account.getId(), newBalance);
        return transactionMapper.toResponse(savedTx);
    }
}
