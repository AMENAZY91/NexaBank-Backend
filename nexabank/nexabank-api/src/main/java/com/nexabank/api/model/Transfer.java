package com.nexabank.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad de dominio — documento Firestore {@code transfers}.
 *
 * <p>Las transferencias son operaciones de negocio, no simples documentos CRUD.
 * Deben soportar idempotencia via {@code idempotencyKey}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    private String id;
    private String sourceAccountId;
    private String destinationAccountId;
    private String sourceUserId;
    private String destinationUserId;
    private BigDecimal amount;
    private String currency;

    @Builder.Default
    private String status = "PENDING";

    private String idempotencyKey;
    private String description;
    private Instant createdAt;
    private Instant completedAt;
}
