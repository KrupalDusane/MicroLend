package com.microlend.repayment.exception;

public class LoanNotDisbursedException extends RuntimeException {
    public LoanNotDisbursedException(String message) {
        super(message);
    }
}
