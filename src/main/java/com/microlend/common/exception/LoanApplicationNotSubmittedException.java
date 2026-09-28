package com.microlend.common.exception;

public class LoanApplicationNotSubmittedException extends RuntimeException {
    public LoanApplicationNotSubmittedException(String message) {
        super(message);
    }
}
