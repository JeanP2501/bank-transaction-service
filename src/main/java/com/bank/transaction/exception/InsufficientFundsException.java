package com.bank.transaction.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when there are insufficient funds
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(BigDecimal requested, BigDecimal available) {
        super(String.format("Insufficient funds. Requested: %s, Available: %s",
                requested, available));
    }
}
