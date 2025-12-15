package com.bank.transaction.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccBalanceUpdRequest {

    private BigDecimal balance;

}
