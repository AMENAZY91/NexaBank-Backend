package com.nexabank.api.dto.beneficiary;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de request para PUT /api/v1/beneficiaries/{id}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBeneficiaryRequest {

    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 50, message = "Alias must be at most 50 characters")
    private String alias;

    private String accountReference;
}
