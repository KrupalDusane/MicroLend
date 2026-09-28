package com.microlend.common.exception;

public class LoanNotApprovedForDisbursementException extends RuntimeException {

    public LoanNotApprovedForDisbursementException(String message) {
        super(message);
    }

}
