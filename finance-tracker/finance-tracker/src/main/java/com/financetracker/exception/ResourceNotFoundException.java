package com.financetracker.exception;

/**
 * Thrown when a requested resource is not found in the database.
 * Example: "Transaction with id 5 not found"
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
