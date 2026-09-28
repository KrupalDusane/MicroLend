package com.microlend.common.exception;

public class LoanNotEligibleForApprovalException extends RuntimeException {
    public LoanNotEligibleForApprovalException(String message) {
        super(message);
    }
}
