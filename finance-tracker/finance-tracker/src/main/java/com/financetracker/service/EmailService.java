package com.financetracker.service;

import com.financetracker.model.MonthlySummary;
import com.financetracker.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.Month;

/**
 * Email Service — sends monthly financial summary emails to users.
 *
 * Uses JavaMailSender (Spring Boot's built-in email client).
 * Non-negotiable skill: sending emails from Spring Boot.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send the monthly summary email with AI advice.
     */
    public void sendMonthlySummaryEmail(User user, MonthlySummary summary) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject(String.format("💰 Your %s %d Finance Summary is Ready!",
                    Month.of(summary.getMonth()).name(), summary.getYear()));
            helper.setText(buildEmailHtml(user, summary), true); // true = HTML email

            mailSender.send(message);
            log.info("Summary email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    /**
     * Send welcome email when user registers.
     */
    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("👋 Welcome to AI Finance Tracker!");
            helper.setText(buildWelcomeHtml(user), true);

            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Welcome email failed for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    // ── HTML email templates ──────────────────────────────────────────────────

    private String buildEmailHtml(User user, MonthlySummary summary) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px;">
                <h2 style="color: #2563eb;">💰 Your %s %d Finance Summary</h2>
                <p>Hi <strong>%s</strong>! Here's your monthly financial report:</p>
                
                <table style="width:100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr style="background: #f0fdf4;">
                        <td style="padding: 12px; border: 1px solid #ddd;">💵 Total Income</td>
                        <td style="padding: 12px; border: 1px solid #ddd; color: #16a34a;">
                            <strong>₹%.2f</strong></td>
                    </tr>
                    <tr style="background: #fef2f2;">
                        <td style="padding: 12px; border: 1px solid #ddd;">💸 Total Expenses</td>
                        <td style="padding: 12px; border: 1px solid #ddd; color: #dc2626;">
                            <strong>₹%.2f</strong></td>
                    </tr>
                    <tr style="background: #eff6ff;">
                        <td style="padding: 12px; border: 1px solid #ddd;">🏦 Savings</td>
                        <td style="padding: 12px; border: 1px solid #ddd; color: #2563eb;">
                            <strong>₹%.2f</strong></td>
                    </tr>
                </table>
                
                <h3 style="color: #7c3aed;">🤖 AI Financial Advice</h3>
                <div style="background: #faf5ff; padding: 16px; border-radius: 8px;
                            border-left: 4px solid #7c3aed;">
                    %s
                </div>
                
                <p style="color: #6b7280; font-size: 12px; margin-top: 30px;">
                    This email was generated automatically by AI Finance Tracker.
                </p>
            </body>
            </html>
            """,
            Month.of(summary.getMonth()).name(), summary.getYear(),
            user.getName(),
            summary.getTotalIncome(),
            summary.getTotalExpenses(),
            summary.getSavings(),
            summary.getAiAdvice().replace("\n", "<br/>")
        );
    }

    private String buildWelcomeHtml(User user) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px;">
                <h2 style="color: #2563eb;">👋 Welcome to AI Finance Tracker!</h2>
                <p>Hi <strong>%s</strong>, your account has been created successfully.</p>
                <p>Start adding your transactions and get personalised AI-powered saving tips
                   at the end of every month!</p>
                <p style="color: #6b7280;">Happy saving! 💰</p>
            </body>
            </html>
            """, user.getName());
    }
}
