package com.financetracker.repository;

import com.financetracker.model.Category;
import com.financetracker.model.Transaction;
import com.financetracker.model.TransactionType;
import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Database access layer for Transaction.
 * Contains custom JPQL queries for filtering and aggregation.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Get all transactions for a user, newest first
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    // Get transactions filtered by type (INCOME or EXPENSE)
    List<Transaction> findByUserAndTypeOrderByTransactionDateDesc(User user, TransactionType type);

    // Get transactions filtered by category
    List<Transaction> findByUserAndCategoryOrderByTransactionDateDesc(User user, Category category);

    // Get transactions for a specific month and year
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND MONTH(t.transactionDate) = :month " +
           "AND YEAR(t.transactionDate) = :year " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndMonthAndYear(
            @Param("user") User user,
            @Param("month") int month,
            @Param("year") int year
    );

    // Sum total amount by type for a given month (used for monthly summary)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user " +
           "AND t.type = :type " +
           "AND MONTH(t.transactionDate) = :month " +
           "AND YEAR(t.transactionDate) = :year")
    BigDecimal sumByUserAndTypeAndMonthAndYear(
            @Param("user") User user,
            @Param("type") TransactionType type,
            @Param("month") int month,
            @Param("year") int year
    );

    // Get all users who have at least one transaction this month (for scheduler)
    @Query("SELECT DISTINCT t.user FROM Transaction t " +
           "WHERE MONTH(t.transactionDate) = :month AND YEAR(t.transactionDate) = :year")
    List<User> findUsersWithTransactionsInMonth(
            @Param("month") int month,
            @Param("year") int year
    );
}
