package com.nexabank.api.controller;

import com.nexabank.api.dto.transaction.CreateTransactionRequest;
import com.nexabank.api.dto.transaction.TransactionResponse;
import com.nexabank.api.dto.transaction.TransactionFilterRequest;
import com.nexabank.api.service.TransactionService;
import com.nexabank.api.service.AccountService;
import com.nexabank.api.security.SecurityUtils;
import com.nexabank.api.dto.common.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final SecurityUtils securityUtils;

    public TransactionController(TransactionService transactionService,
                                  AccountService accountService,
                                  SecurityUtils securityUtils) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (accountId == null || accountId.isBlank()) {
            userId = securityUtils.getCurrentUserId();
        }
        List<TransactionResponse> transactions = transactionService.getTransactions(accountId, userId);
        PageResponse<TransactionResponse> pageResponse = PageResponse.of(transactions, page, size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        TransactionResponse transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody CreateTransactionRequest request) {
        String userId = securityUtils.getCurrentUserId();
        TransactionResponse created = transactionService.createTransaction(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
