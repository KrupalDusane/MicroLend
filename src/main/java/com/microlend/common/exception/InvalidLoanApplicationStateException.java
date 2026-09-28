package com.microlend.common.exception;

public class InvalidLoanApplicationStateException extends RuntimeException {
    public InvalidLoanApplicationStateException(String message) {
        super(message);
    }
}
