package com.bank.transaction.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for credit information from Credit Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {

    private String id;
    private String creditNumber;
    private String creditType;
    private String customerId;
    private BigDecimal creditAmount;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal interestRate;
    private BigDecimal minimumPayment;
    private Integer paymentDueDay;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
