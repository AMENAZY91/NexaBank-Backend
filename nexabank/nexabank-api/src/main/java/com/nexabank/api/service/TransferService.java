package com.nexabank.api.service;

import com.nexabank.api.dto.transfer.CreateTransferRequest;
import com.nexabank.api.dto.transfer.TransferResponse;
import com.nexabank.api.exception.InsufficientFundsException;
import com.nexabank.api.exception.InvalidTransactionException;
import com.nexabank.api.exception.ResourceNotFoundException;
import com.nexabank.api.mapper.TransferMapper;
import com.nexabank.api.model.Account;
import com.nexabank.api.model.AuditLog;
import com.nexabank.api.model.IdempotencyKey;
import com.nexabank.api.model.Transaction;
import com.nexabank.api.model.Transfer;
import com.nexabank.api.repository.AccountRepository;
import com.nexabank.api.repository.AuditLogRepository;
import com.nexabank.api.repository.IdempotencyRepository;
import com.nexabank.api.repository.TransactionRepository;
import com.nexabank.api.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransferMapper transferMapper;

    public TransferService(TransferRepository transferRepository,
                           AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           IdempotencyRepository idempotencyRepository,
                           AuditLogRepository auditLogRepository,
                           TransferMapper transferMapper) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.auditLogRepository = auditLogRepository;
        this.transferMapper = transferMapper;
    }

    public List<TransferResponse> getTransfersByUser(String userId) {
        return transferRepository.findByUserId(userId).stream()
                .map(transferMapper::toResponse)
                .toList();
    }

    public TransferResponse getTransferById(String id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with ID: " + id));
        return transferMapper.toResponse(transfer);
    }

    public TransferResponse executeTransfer(CreateTransferRequest request, String idempotencyKeyHeader, String userId) {
        // 1. Validar idempotencia si se proporciona key
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            Optional<IdempotencyKey> existingKey = idempotencyRepository.findByKey(idempotencyKeyHeader);
            if (existingKey.isPresent()) {
                IdempotencyKey ik = existingKey.get();
                if ("COMPLETED".equals(ik.getStatus()) && ik.getResponseReference() != null) {
                    log.info("Idempotent request detected for key {}. Returning existing transfer {}", idempotencyKeyHeader, ik.getResponseReference());
                    return getTransferById(ik.getResponseReference());
                }
            }
        }

        // 2. Validaciones de cuentas y montos
        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new InvalidTransactionException("Source and destination accounts must be different");
        }

        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found: " + request.getSourceAccountId()));

        Account destAccount = accountRepository.findById(request.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found: " + request.getDestinationAccountId()));

        if (!sourceAccount.getCurrency().equalsIgnoreCase(destAccount.getCurrency()) ||
                !sourceAccount.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new InvalidTransactionException("Currency mismatch between accounts or transfer currency");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds in source account " + sourceAccount.getId());
        }

        // 3. Debitar origen y acreditar destino
        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(request.getAmount());
        BigDecimal newDestBalance = destAccount.getBalance().add(request.getAmount());

        accountRepository.updateBalance(sourceAccount.getId(), newSourceBalance);
        accountRepository.updateBalance(destAccount.getId(), newDestBalance);

        Instant now = Instant.now();

        // 4. Crear transacciones en ambos lados
        Transaction debitTx = transactionRepository.save(Transaction.builder()
                .accountId(sourceAccount.getId())
                .userId(sourceAccount.getUserId())
                .type("EXPENSE")
                .category("TRANSFER")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description("Transfer to " + destAccount.getId() + ": " + (request.getDescription() != null ? request.getDescription() : ""))
                .status("COMPLETED")
                .timestamp(now)
                .createdAt(now)
                .build());

        Transaction creditTx = transactionRepository.save(Transaction.builder()
                .accountId(destAccount.getId())
                .userId(destAccount.getUserId())
                .type("INCOME")
                .category("TRANSFER")
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description("Transfer from " + sourceAccount.getId() + ": " + (request.getDescription() != null ? request.getDescription() : ""))
                .status("COMPLETED")
                .timestamp(now)
                .createdAt(now)
                .build());

        // 5. Crear y guardar Transfer
        Transfer transfer = Transfer.builder()
                .sourceAccountId(sourceAccount.getId())
                .destinationAccountId(destAccount.getId())
                .sourceUserId(sourceAccount.getUserId())
                .destinationUserId(destAccount.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("COMPLETED")
                .idempotencyKey(idempotencyKeyHeader)
                .description(request.getDescription())
                .createdAt(now)
                .completedAt(now)
                .build();

        Transfer savedTransfer = transferRepository.save(transfer);

        // 6. Registrar idempotencia
        if (idempotencyKeyHeader != null && !idempotencyKeyHeader.isBlank()) {
            idempotencyRepository.save(IdempotencyKey.builder()
                    .key(idempotencyKeyHeader)
                    .userId(userId != null ? userId : sourceAccount.getUserId())
                    .operation("TRANSFER")
                    .status("COMPLETED")
                    .responseReference(savedTransfer.getId())
                    .createdAt(now)
                    .expiresAt(now.plus(24, ChronoUnit.HOURS))
                    .build());
        }

        // 7. Auditoría
        auditLogRepository.save(AuditLog.builder()
                .userId(userId != null ? userId : sourceAccount.getUserId())
                .action("TRANSFER")
                .resourceType("TRANSFER")
                .resourceId(savedTransfer.getId())
                .status("SUCCESS")
                .timestamp(now)
                .metadata(Map.of(
                        "sourceAccountId", sourceAccount.getId(),
                        "destinationAccountId", destAccount.getId(),
                        "amount", request.getAmount().doubleValue(),
                        "debitTransactionId", debitTx.getId(),
                        "creditTransactionId", creditTx.getId()
                ))
                .build());

        log.info("Transfer {} completed successfully: {} {} from {} to {}",
                savedTransfer.getId(), request.getAmount(), request.getCurrency(), sourceAccount.getId(), destAccount.getId());

        return transferMapper.toResponse(savedTransfer);
    }
}
