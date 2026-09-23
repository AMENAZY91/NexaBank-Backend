package com.nexabank.api.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de respuesta para transacciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String id;
    private String accountId;
    private String type;
    private String category;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    private Instant timestamp;
    private Instant createdAt;
}
