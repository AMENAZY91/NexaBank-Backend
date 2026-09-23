package com.nexabank.api.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de respuesta para cuentas.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String id;
    private String type;
    private String currency;
    private String name;
    private BigDecimal balance;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
