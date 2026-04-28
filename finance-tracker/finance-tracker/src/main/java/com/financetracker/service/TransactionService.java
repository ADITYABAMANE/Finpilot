package com.financetracker.service;

import com.financetracker.dto.TransactionRequest;
import com.financetracker.dto.TransactionResponse;
import com.financetracker.exception.BusinessException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.model.*;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction Service — all business logic for managing transactions.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Add a new transaction for the logged-in user.
     */
    public TransactionResponse addTransaction(TransactionRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(request.getType())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Get all transactions for the logged-in user.
     * Optionally filter by type (INCOME/EXPENSE) or category.
     */
    public List<TransactionResponse> getTransactions(
            String userEmail,
            TransactionType type,
            Category category
    ) {
        User user = getUserByEmail(userEmail);
        List<Transaction> transactions;

        if (type != null) {
            transactions = transactionRepository.findByUserAndTypeOrderByTransactionDateDesc(user, type);
        } else if (category != null) {
            transactions = transactionRepository.findByUserAndCategoryOrderByTransactionDateDesc(user, category);
        } else {
            transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        }

        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Get a single transaction by ID.
     * Ensures the transaction belongs to the requesting user.
     */
    public TransactionResponse getTransactionById(Long id, String userEmail) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Security check — users can only see their own transactions
        if (!transaction.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("You can only view your own transactions");
        }

        return mapToResponse(transaction);
    }

    /**
     * Update an existing transaction.
     */
    public TransactionResponse updateTransaction(Long id, TransactionRequest request, String userEmail) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("You can only update your own transactions");
        }

        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());

        return mapToResponse(transactionRepository.save(transaction));
    }

    /**
     * Delete a transaction.
     */
    public void deleteTransaction(Long id, String userEmail) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("You can only delete your own transactions");
        }

        transactionRepository.delete(transaction);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public TransactionResponse mapToResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType(),
                t.getCategory(),
                t.getAmount(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getCreatedAt()
        );
    }
}
