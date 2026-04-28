package com.financetracker.service;

import com.financetracker.dto.SummaryResponse;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.model.*;
import com.financetracker.repository.MonthlySummaryRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Summary Service — generates monthly financial summaries with AI advice.
 *
 * Called by:
 *   1. The scheduled job (automatically every month)
 *   2. The user manually via GET /api/summary?month=5&year=2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final TransactionRepository transactionRepository;
    private final MonthlySummaryRepository summaryRepository;
    private final UserRepository userRepository;
    private final OpenAiService openAiService;
    private final EmailService emailService;

    /**
     * Get or generate a monthly summary for the logged-in user.
     * If summary already exists for that month, returns the cached one.
     */
    public SummaryResponse getSummary(String userEmail, int month, int year) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if summary already exists
        return summaryRepository.findByUserAndMonthAndYear(user, month, year)
                .map(this::mapToResponse)
                .orElseGet(() -> generateAndSave(user, month, year));
    }

    /**
     * Get all past summaries for the logged-in user.
     */
    public List<SummaryResponse> getAllSummaries(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return summaryRepository.findByUserOrderByYearDescMonthDesc(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Generate summary for a user for a given month.
     * Called by the scheduler for ALL users at end of month.
     */
    public void generateSummaryForUser(User user, int month, int year) {
        // Skip if already generated
        if (summaryRepository.findByUserAndMonthAndYear(user, month, year).isPresent()) {
            log.info("Summary already exists for user {} month {}/{}", user.getEmail(), month, year);
            return;
        }
        generateAndSave(user, month, year);
    }

    // ── Core generation logic ─────────────────────────────────────────────────

    private SummaryResponse generateAndSave(User user, int month, int year) {
        // 1. Get total income and expenses for the month
        BigDecimal totalIncome = transactionRepository.sumByUserAndTypeAndMonthAndYear(
                user, TransactionType.INCOME, month, year);
        BigDecimal totalExpenses = transactionRepository.sumByUserAndTypeAndMonthAndYear(
                user, TransactionType.EXPENSE, month, year);
        BigDecimal savings = totalIncome.subtract(totalExpenses);

        // 2. Build category breakdown for expenses
        List<Transaction> monthlyTransactions = transactionRepository
                .findByUserAndMonthAndYear(user, month, year);

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();
        monthlyTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .forEach(t -> categoryBreakdown.merge(
                        t.getCategory().name(),
                        t.getAmount(),
                        BigDecimal::add
                ));

        // 3. Call OpenAI for personalised advice
        log.info("Generating AI advice for user: {}", user.getEmail());
        String aiAdvice = openAiService.generateFinancialAdvice(
                totalIncome, totalExpenses, categoryBreakdown, user.getName());

        // 4. Save summary to DB
        MonthlySummary summary = MonthlySummary.builder()
                .user(user)
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .savings(savings)
                .aiAdvice(aiAdvice)
                .generatedAt(LocalDateTime.now())
                .build();

        MonthlySummary saved = summaryRepository.save(summary);

        // 5. Send email notification
        try {
            emailService.sendMonthlySummaryEmail(user, saved);
        } catch (Exception e) {
            log.warn("Email sending failed for {}: {}", user.getEmail(), e.getMessage());
        }

        return mapToResponse(saved);
    }

    private SummaryResponse mapToResponse(MonthlySummary s) {
        return new SummaryResponse(
                s.getMonth(), s.getYear(),
                s.getTotalIncome(), s.getTotalExpenses(),
                s.getSavings(), s.getAiAdvice(), s.getGeneratedAt()
        );
    }
}
