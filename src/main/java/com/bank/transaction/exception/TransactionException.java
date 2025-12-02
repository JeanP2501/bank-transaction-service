package com.bank.transaction.exception;

/**
 * Exception thrown when a transaction fails
 */
public class TransactionException extends RuntimeException {

    public TransactionException(String message) {
        super(message);
    }
}
