package com.microlend.prepayment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Quote for full loan settlement/foreclosure")
public class SettlementQuoteResponse {

    @Schema(description = "Remaining unpaid principal", example = "87915.90")
    private BigDecimal outstandingPrincipal;
    
    @Schema(description = "Remaining unpaid interest", example = "833.33")
    private BigDecimal outstandingInterest;
    
    @Schema(description = "Total active penalties", example = "500.00")
    private BigDecimal activePenalties;
    
    @Schema(description = "Total amount required to settle the loan", example = "89249.23")
    private BigDecimal settlementAmount;
    
    @Schema(description = "Timestamp when the quote was generated", example = "2026-09-13T10:00:00")
    private LocalDateTime calculatedAt;

    public SettlementQuoteResponse() {
    }

    public SettlementQuoteResponse(BigDecimal outstandingPrincipal, BigDecimal outstandingInterest, BigDecimal activePenalties, BigDecimal settlementAmount, LocalDateTime calculatedAt) {
        this.outstandingPrincipal = outstandingPrincipal;
        this.outstandingInterest = outstandingInterest;
        this.activePenalties = activePenalties;
        this.settlementAmount = settlementAmount;
        this.calculatedAt = calculatedAt;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public BigDecimal getOutstandingInterest() {
        return outstandingInterest;
    }

    public void setOutstandingInterest(BigDecimal outstandingInterest) {
        this.outstandingInterest = outstandingInterest;
    }

    public BigDecimal getActivePenalties() {
        return activePenalties;
    }

    public void setActivePenalties(BigDecimal activePenalties) {
        this.activePenalties = activePenalties;
    }

    public BigDecimal getSettlementAmount() {
        return settlementAmount;
    }

    public void setSettlementAmount(BigDecimal settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }
}
