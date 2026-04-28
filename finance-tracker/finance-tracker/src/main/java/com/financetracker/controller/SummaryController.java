package com.financetracker.controller;

import com.financetracker.dto.ApiResponse;
import com.financetracker.dto.SummaryResponse;
import com.financetracker.scheduler.MonthlySummaryScheduler;
import com.financetracker.service.SummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Summary Controller — endpoints to view AI-generated financial summaries.
 */
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
@Tag(name = "Summary", description = "View AI-generated monthly financial summaries")
@SecurityRequirement(name = "bearerAuth")
public class SummaryController {

    private final SummaryService summaryService;

    /**
     * GET /api/summary?month=5&year=2025
     * Get (or generate) summary for a specific month.
     * If it doesn't exist yet, it generates one on-demand.
     */
    @GetMapping
    @Operation(summary = "Get monthly AI summary (generates if not exists)")
    public ResponseEntity<ApiResponse<SummaryResponse>> getSummary(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Default to current month if not specified
        LocalDate now = LocalDate.now();
        int targetMonth = month > 0 ? month : now.getMonthValue();
        int targetYear  = year  > 0 ? year  : now.getYear();

        SummaryResponse summary = summaryService.getSummary(
                userDetails.getUsername(), targetMonth, targetYear);
        return ResponseEntity.ok(ApiResponse.success("Summary fetched", summary));
    }

    /**
     * GET /api/summary/all
     * Get all past summaries for the logged-in user.
     */
    @GetMapping("/all")
    @Operation(summary = "Get all past monthly summaries")
    public ResponseEntity<ApiResponse<List<SummaryResponse>>> getAllSummaries(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<SummaryResponse> summaries = summaryService.getAllSummaries(
                userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("All summaries fetched", summaries));
    }
}


/**
 * Admin Controller — admin-only endpoints.
 * Requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only endpoints")
@SecurityRequirement(name = "bearerAuth")
class AdminController {

    private final MonthlySummaryScheduler scheduler;

    /**
     * POST /api/admin/trigger-summary?month=5&year=2025
     * Manually trigger summary generation (for testing — no need to wait for month end).
     * ADMIN only.
     */
    @PostMapping("/trigger-summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually trigger monthly summary generation (Admin only)")
    public ResponseEntity<ApiResponse<Void>> triggerSummary(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        LocalDate now = LocalDate.now();
        int targetMonth = month > 0 ? month : now.getMonthValue();
        int targetYear  = year  > 0 ? year  : now.getYear();

        scheduler.triggerManually(targetMonth, targetYear);
        return ResponseEntity.ok(ApiResponse.success(
                "Summary generation triggered for " + targetMonth + "/" + targetYear, null));
    }
}
