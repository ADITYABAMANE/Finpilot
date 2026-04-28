package com.financetracker.repository;

import com.financetracker.model.MonthlySummary;
import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Database access layer for MonthlySummary.
 */
@Repository
public interface MonthlySummaryRepository extends JpaRepository<MonthlySummary, Long> {

    // Find summary for a specific user, month, year
    Optional<MonthlySummary> findByUserAndMonthAndYear(User user, int month, int year);

    // Get all summaries for a user, newest first
    List<MonthlySummary> findByUserOrderByYearDescMonthDesc(User user);
}
