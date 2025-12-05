package com.bank.transaction.exception;

/**
 * Exception thrown when a credit is not found
 */
public class CreditNotFoundException extends RuntimeException {

    public CreditNotFoundException(String id) {
        super("Credit not found with id: " + id);
    }

    public CreditNotFoundException(String field, String value) {
        super(String.format("Credit not found with %s: %s", field, value));
    }
}
