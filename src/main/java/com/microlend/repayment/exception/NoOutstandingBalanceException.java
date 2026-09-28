package com.microlend.repayment.exception;

public class NoOutstandingBalanceException extends RuntimeException {
    public NoOutstandingBalanceException(String message) {
        super(message);
    }
}
