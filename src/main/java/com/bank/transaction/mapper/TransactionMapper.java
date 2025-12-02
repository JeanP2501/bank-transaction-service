package com.bank.transaction.mapper;

import com.bank.transaction.model.dto.TransactionResponse;
import com.bank.transaction.model.entity.Transaction;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Transaction entity and DTOs
 */
@Component
public class TransactionMapper {

    /**
     * Convert Transaction entity to TransactionResponse
     * @param transaction the transaction entity
     * @return TransactionResponse DTO
     */
    public TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .accountId(transaction.getAccountId())
                .creditId(transaction.getCreditId())
                .customerId(transaction.getCustomerId())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .balanceAfter(transaction.getBalanceAfter())
                .errorMessage(transaction.getErrorMessage())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
