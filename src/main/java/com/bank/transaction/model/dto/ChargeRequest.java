package com.bank.transaction.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for credit card charge request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRequest {

    @NotBlank(message = "Credit ID is required")
    private String creditId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}
