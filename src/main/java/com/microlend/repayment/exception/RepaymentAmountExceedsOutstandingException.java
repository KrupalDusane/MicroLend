package com.microlend.repayment.exception;

public class RepaymentAmountExceedsOutstandingException extends RuntimeException {
    public RepaymentAmountExceedsOutstandingException(String message) {
        super(message);
    }
}
