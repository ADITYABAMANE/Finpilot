package com.financetracker.scheduler;

import com.financetracker.model.User;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.service.SummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Monthly Summary Scheduler.
 *
 * Automatically runs at the end of every month and generates
 * AI-powered financial summaries for all active users.
 *
 * Non-negotiable skill: @Scheduled background jobs in Spring Boot.
 *
 * Cron expression "0 0 8 1 * ?" means:
 *   - second: 0
 *   - minute: 0
 *   - hour: 8 (8 AM)
 *   - day of month: 1 (first day of every month)
 *   - month: * (every month)
 *   Runs on the 1st of every month at 8 AM
 *   → generates summary for the PREVIOUS month
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonthlySummaryScheduler {

    private final TransactionRepository transactionRepository;
    private final SummaryService summaryService;

    /**
     * Runs on the 1st of every month at 8:00 AM.
     * Generates summaries for all users who had transactions last month.
     */
    @Scheduled(cron = "0 0 8 1 * ?")
    public void generateMonthlySummaries() {
        // Get last month's date
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int month = lastMonth.getMonthValue();
        int year = lastMonth.getYear();

        log.info("🗓️ Running monthly summary scheduler for {}/{}", month, year);

        // Find all users who had transactions last month
        List<User> activeUsers = transactionRepository
                .findUsersWithTransactionsInMonth(month, year);

        log.info("Found {} active users for {}/{}", activeUsers.size(), month, year);

        // Generate summary for each user
        for (User user : activeUsers) {
            try {
                log.info("Generating summary for user: {}", user.getEmail());
                summaryService.generateSummaryForUser(user, month, year);
                log.info("✅ Summary generated for: {}", user.getEmail());
            } catch (Exception e) {
                // Don't stop processing other users if one fails
                log.error("❌ Failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        }

        log.info("✅ Monthly summary generation complete for {}/{}", month, year);
    }

    /**
     * FOR TESTING: Run this manually by calling POST /api/admin/trigger-summary
     * instead of waiting for the first of the month.
     */
    public void triggerManually(int month, int year) {
        log.info("Manual trigger: generating summaries for {}/{}", month, year);
        List<User> users = transactionRepository.findUsersWithTransactionsInMonth(month, year);
        users.forEach(user -> summaryService.generateSummaryForUser(user, month, year));
    }
}
