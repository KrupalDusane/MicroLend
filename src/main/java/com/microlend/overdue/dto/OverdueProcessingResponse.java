package com.microlend.overdue.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of the overdue batch processing job")
public class OverdueProcessingResponse {
    @Schema(description = "Number of overdue installments processed", example = "10")
    private int processedInstallments;
    
    @Schema(description = "Number of new penalties created", example = "5")
    private int newPenaltiesCreated;
    
    @Schema(description = "Number of installments already processed today", example = "5")
    private int alreadyProcessed;
    
    @Schema(description = "Total amount of penalties created in this run", example = "2500.00")
    private BigDecimal totalPenaltyCreated;
    
    @Schema(description = "Timestamp when the processing completed", example = "2026-09-13T10:00:00")
    private LocalDateTime processedAt;

    public OverdueProcessingResponse() {
    }

    public OverdueProcessingResponse(int processedInstallments, int newPenaltiesCreated, int alreadyProcessed, BigDecimal totalPenaltyCreated, LocalDateTime processedAt) {
        this.processedInstallments = processedInstallments;
        this.newPenaltiesCreated = newPenaltiesCreated;
        this.alreadyProcessed = alreadyProcessed;
        this.totalPenaltyCreated = totalPenaltyCreated;
        this.processedAt = processedAt;
    }

    public int getProcessedInstallments() {
        return processedInstallments;
    }

    public void setProcessedInstallments(int processedInstallments) {
        this.processedInstallments = processedInstallments;
    }

    public int getNewPenaltiesCreated() {
        return newPenaltiesCreated;
    }

    public void setNewPenaltiesCreated(int newPenaltiesCreated) {
        this.newPenaltiesCreated = newPenaltiesCreated;
    }

    public int getAlreadyProcessed() {
        return alreadyProcessed;
    }

    public void setAlreadyProcessed(int alreadyProcessed) {
        this.alreadyProcessed = alreadyProcessed;
    }

    public BigDecimal getTotalPenaltyCreated() {
        return totalPenaltyCreated;
    }

    public void setTotalPenaltyCreated(BigDecimal totalPenaltyCreated) {
        this.totalPenaltyCreated = totalPenaltyCreated;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
