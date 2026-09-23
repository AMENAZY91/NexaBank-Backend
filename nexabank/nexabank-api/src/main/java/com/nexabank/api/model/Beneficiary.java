package com.nexabank.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Entidad de dominio — documento Firestore {@code beneficiaries}.
 *
 * <p>No almacenar información bancaria innecesaria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Beneficiary {

    private String id;
    private String userId;
    private String name;
    private String alias;
    private String accountReference;

    @Builder.Default
    private String status = "ACTIVE";

    private Instant createdAt;
    private Instant updatedAt;
}
