package com.financetracker.dto;

import com.financetracker.model.Category;
import com.financetracker.model.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// ─────────────────────────────────────────────────────────────────────────────
//  AUTH DTOs
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Request body for POST /api/auth/register
 */
class RegisterRequest {
    @NotBlank(message = "Name is required")
    public String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    public String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @NotBlank(message = "Password is required")
    public String password;
}

/**
 * Request body for POST /api/auth/login
 */
class LoginRequest {
    @NotBlank(message = "Email is required")
    public String email;

    @NotBlank(message = "Password is required")
    public String password;
}

/**
 * Response for login/register — contains the JWT token
 */
class AuthResponse {
    public String token;
    public String name;
    public String email;
    public String message;

    public AuthResponse(String token, String name, String email, String message) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.message = message;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TRANSACTION DTOs
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Request body for POST /api/transactions
 */
class TransactionRequest {
    @NotNull(message = "Type is required (INCOME or EXPENSE)")
    public TransactionType type;

    @NotNull(message = "Category is required")
    public Category category;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    public BigDecimal amount;

    public String description;

    @NotNull(message = "Transaction date is required")
    public LocalDate transactionDate;
}

/**
 * Response object for a transaction
 */
class TransactionResponse {
    public Long id;
    public TransactionType type;
    public Category category;
    public BigDecimal amount;
    public String description;
    public LocalDate transactionDate;
    public LocalDateTime createdAt;
}

// ─────────────────────────────────────────────────────────────────────────────
//  SUMMARY DTOs
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Response for the monthly summary endpoint
 */
class SummaryResponse {
    public int month;
    public int year;
    public BigDecimal totalIncome;
    public BigDecimal totalExpenses;
    public BigDecimal savings;
    public String aiAdvice;
    public LocalDateTime generatedAt;
}

// ─────────────────────────────────────────────────────────────────────────────
//  GENERIC API RESPONSE WRAPPER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Standard API response wrapper for consistent response format.
 *
 * Example:
 * {
 *   "success": true,
 *   "message": "Transaction added",
 *   "data": { ... }
 * }
 */
class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
