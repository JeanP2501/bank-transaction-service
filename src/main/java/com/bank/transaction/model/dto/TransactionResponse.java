package com.bank.transaction.model.dto;

import com.bank.transaction.model.enums.TransactionStatus;
import com.bank.transaction.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String accountId;
    private String creditId;
    private String customerId;
    private TransactionStatus status;
    private String description;
    private BigDecimal balanceAfter;
    private String errorMessage;
    private LocalDateTime createdAt;
    private BigDecimal commission;
}
