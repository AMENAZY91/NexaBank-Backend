package com.nexabank.api.mapper;

import com.nexabank.api.dto.beneficiary.BeneficiaryResponse;
import com.nexabank.api.dto.beneficiary.CreateBeneficiaryRequest;
import com.nexabank.api.dto.beneficiary.UpdateBeneficiaryRequest;
import com.nexabank.api.model.Beneficiary;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper para transformaciones Beneficiary ↔ DTO.
 */
@Component
public class BeneficiaryMapper {

    public BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .name(beneficiary.getName())
                .alias(beneficiary.getAlias())
                .accountReference(beneficiary.getAccountReference())
                .status(beneficiary.getStatus())
                .createdAt(beneficiary.getCreatedAt())
                .updatedAt(beneficiary.getUpdatedAt())
                .build();
    }

    public Beneficiary fromCreateRequest(CreateBeneficiaryRequest request, String userId) {
        Instant now = Instant.now();
        return Beneficiary.builder()
                .userId(userId)
                .name(request.getName())
                .alias(request.getAlias())
                .accountReference(request.getAccountReference())
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void updateFromRequest(UpdateBeneficiaryRequest request, Beneficiary beneficiary) {
        if (request.getName() != null) {
            beneficiary.setName(request.getName());
        }
        if (request.getAlias() != null) {
            beneficiary.setAlias(request.getAlias());
        }
        if (request.getAccountReference() != null) {
            beneficiary.setAccountReference(request.getAccountReference());
        }
        beneficiary.setUpdatedAt(Instant.now());
    }
}
