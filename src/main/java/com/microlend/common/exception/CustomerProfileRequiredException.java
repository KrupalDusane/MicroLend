package com.microlend.common.exception;

public class CustomerProfileRequiredException extends RuntimeException {
    public CustomerProfileRequiredException(String message) {
        super(message);
    }
}
