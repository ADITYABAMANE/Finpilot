package com.financetracker.exception;

/**
 * Thrown when a business rule is violated.
 * Example: "Email already registered", "Cannot delete another user's transaction"
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
