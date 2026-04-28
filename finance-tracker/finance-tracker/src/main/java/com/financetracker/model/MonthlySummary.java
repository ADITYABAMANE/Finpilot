package com.financetracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stores the AI-generated monthly financial summary for each user.
 * Created automatically every month by the scheduler.
 */
@Entity
@Table(name = "monthly_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int month;  // 1–12
    private int year;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalIncome;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalExpenses;

    @Column(precision = 10, scale = 2)
    private BigDecimal savings;

    // The AI-generated advice from OpenAI — stored as text
    @Column(columnDefinition = "TEXT")
    private String aiAdvice;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
}
