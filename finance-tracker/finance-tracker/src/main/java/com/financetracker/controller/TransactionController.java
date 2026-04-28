package com.financetracker.controller;

import com.financetracker.dto.ApiResponse;
import com.financetracker.dto.TransactionRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.model.Category;
import com.financetracker.model.TransactionType;
import com.financetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Transaction Controller — CRUD endpoints for managing transactions.
 * All endpoints require a valid JWT token (set in Authorization header).
 *
 * How to test: Authorization: Bearer <your_token>
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Manage income and expense transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST /api/transactions
     * Add a new transaction.
     *
     * Request body:
     * {
     *   "type": "EXPENSE",
     *   "category": "FOOD",
     *   "amount": 850.00,
     *   "description": "Swiggy order",
     *   "transactionDate": "2025-05-15"
     * }
     */
    @PostMapping
    @Operation(summary = "Add a new transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> addTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TransactionResponse response = transactionService.addTransaction(
                request, userDetails.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction added successfully", response));
    }

    /**
     * GET /api/transactions
     * Get all transactions. Optional filters: ?type=EXPENSE or ?category=FOOD
     */
    @GetMapping
    @Operation(summary = "Get all transactions (with optional filters)")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Category category,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TransactionResponse> transactions = transactionService.getTransactions(
                userDetails.getUsername(), type, category);
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched", transactions));
    }

    /**
     * GET /api/transactions/{id}
     * Get a single transaction by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        TransactionResponse response = transactionService.getTransactionById(
                id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transaction fetched", response));
    }

    /**
     * PUT /api/transactions/{id}
     * Update an existing transaction.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TransactionResponse response = transactionService.updateTransaction(
                id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transaction updated", response));
    }

    /**
     * DELETE /api/transactions/{id}
     * Delete a transaction.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        transactionService.deleteTransaction(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted", null));
    }
}
