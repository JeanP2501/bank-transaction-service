package com.bank.transaction.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for account information from Account Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String id;
    private String accountNumber;
    private String accountType;
    private String customerId;
    private BigDecimal balance;
    private String currency;
    private BigDecimal maintenanceFee;
    private Integer maxMonthlyTransactions;
    private Integer transactionDay;
    private List<String> holders;
    private List<String> authorizedSigners;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
