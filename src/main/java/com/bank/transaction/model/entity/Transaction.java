package com.bank.transaction.model.entity;

import com.bank.transaction.model.enums.TransactionStatus;
import com.bank.transaction.model.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing all banking transactions
 * Handles deposits, withdrawals, payments, and charges
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    /**
     * Account ID for deposits/withdrawals
     * Null for credit transactions
     */
    private String accountId;

    /**
     * Credit ID for payments/charges
     * Null for account transactions
     */
    private String creditId;

    /**
     * Customer ID who initiated the transaction
     */
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    /**
     * Transaction status
     */
    @NotNull(message = "Status is required")
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    /**
     * Transaction description or reference
     */
    private String description;

    /**
     * Balance after transaction (for reference)
     */
    private BigDecimal balanceAfter;

    /**
     * Error message if transaction failed
     */
    private String errorMessage;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Check if transaction is for an account
     */
    public boolean isAccountTransaction() {
        return transactionType == TransactionType.DEPOSIT ||
                transactionType == TransactionType.WITHDRAWAL;
    }

    /**
     * Check if transaction is for a credit
     */
    public boolean isCreditTransaction() {
        return transactionType == TransactionType.PAYMENT ||
                transactionType == TransactionType.CHARGE;
    }

    /**
     * Commission charged for this transaction.
     * Zero if transaction was free
     */
    private BigDecimal commission = BigDecimal.ZERO;

}
