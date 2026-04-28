package com.financetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Service — calls the GPT API to generate personalised financial advice.
 *
 * This is the AI part of the project.
 * It sends the user's monthly spending summary to GPT and gets back smart saving tips.
 *
 * Non-negotiable skill: LLM API Integration from Java
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model}")
    private String model;

    private final WebClient.Builder webClientBuilder;

    /**
     * Generate personalised financial advice using GPT.
     *
     * @param totalIncome    user's total income for the month
     * @param totalExpenses  user's total expenses for the month
     * @param categorySummary  map of category → amount spent
     * @param userName       user's name for personalisation
     * @return AI-generated advice as a string
     */
    public String generateFinancialAdvice(
            BigDecimal totalIncome,
            BigDecimal totalExpenses,
            Map<String, BigDecimal> categorySummary,
            String userName
    ) {
        String prompt = buildPrompt(totalIncome, totalExpenses, categorySummary, userName);

        try {
            // Build request body in the format OpenAI expects
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt()),
                    Map.of("role", "user",   "content", prompt)
                ),
                "max_tokens", 600,
                "temperature", 0.7
            );

            // Make HTTP POST call to OpenAI
            Map response = webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // synchronous call

            // Extract the AI message from the response
            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            return "Unable to generate AI advice at this time. Please try again later.";
        }
    }

    private String systemPrompt() {
        return """
                You are a friendly and knowledgeable personal finance advisor.
                Your goal is to give practical, actionable, and encouraging advice
                based on the user's monthly spending. Be specific, not generic.
                Keep your response under 200 words. Use bullet points for tips.
                Always end with one motivational sentence.
                """;
    }

    private String buildPrompt(
            BigDecimal income,
            BigDecimal expenses,
            Map<String, BigDecimal> categorySummary,
            String name
    ) {
        BigDecimal savings = income.subtract(expenses);
        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
                ? savings.divide(income, 2, java.math.RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("User: %s\n", name));
        sb.append(String.format("Monthly Income: ₹%.2f\n", income));
        sb.append(String.format("Monthly Expenses: ₹%.2f\n", expenses));
        sb.append(String.format("Savings: ₹%.2f (%.1f%% of income)\n\n", savings, savingsRate));
        sb.append("Spending breakdown by category:\n");

        categorySummary.forEach((category, amount) ->
                sb.append(String.format("  - %s: ₹%.2f\n", category, amount))
        );

        sb.append("\nPlease give personalised financial advice and saving tips.");
        return sb.toString();
    }
}
