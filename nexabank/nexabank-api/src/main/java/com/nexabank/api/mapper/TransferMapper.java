package com.nexabank.api.mapper;

import com.nexabank.api.dto.transfer.TransferResponse;
import com.nexabank.api.model.Transfer;
import org.springframework.stereotype.Component;

/**
 * Mapper para transformaciones Transfer ↔ DTO.
 */
@Component
public class TransferMapper {

    public TransferResponse toResponse(Transfer transfer) {
        return TransferResponse.builder()
                .id(transfer.getId())
                .sourceAccountId(transfer.getSourceAccountId())
                .destinationAccountId(transfer.getDestinationAccountId())
                .amount(transfer.getAmount())
                .currency(transfer.getCurrency())
                .status(transfer.getStatus())
                .description(transfer.getDescription())
                .createdAt(transfer.getCreatedAt())
                .completedAt(transfer.getCompletedAt())
                .build();
    }
}
