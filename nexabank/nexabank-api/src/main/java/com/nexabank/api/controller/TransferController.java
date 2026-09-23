package com.nexabank.api.controller;

import com.nexabank.api.dto.transfer.CreateTransferRequest;
import com.nexabank.api.dto.transfer.TransferResponse;
import com.nexabank.api.service.TransferService;
import com.nexabank.api.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;
    private final SecurityUtils securityUtils;

    public TransferController(TransferService transferService, SecurityUtils securityUtils) {
        this.transferService = transferService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<TransferResponse>> getTransfers() {
        String userId = securityUtils.getCurrentUserId();
        List<TransferResponse> transfers = transferService.getTransfersByUser(userId);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getTransfer(@PathVariable String id) {
        TransferResponse transfer = transferService.getTransferById(id);
        return ResponseEntity.ok(transfer);
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(
            @RequestBody CreateTransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        String userId = securityUtils.getCurrentUserId();
        TransferResponse created = transferService.executeTransfer(request, idempotencyKey, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
