package com.microlend.overdue.dto;

import com.microlend.amortization.entity.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of an overdue installment and its penalties")
public class OverdueInstallmentResponse {
    @Schema(description = "Installment sequence number", example = "1")
    private Integer installmentNumber;
    
    @Schema(description = "Original due date of the installment", example = "2026-08-13")
    private LocalDate dueDate;
    
    @Schema(description = "Number of days the installment is overdue", example = "30")
    private Integer daysOverdue;
    
    @Schema(description = "Remaining unpaid principal for this installment", example = "7958.26")
    private BigDecimal outstandingPrincipal;
    
    @Schema(description = "Total penalty amount accrued", example = "500.00")
    private BigDecimal penaltyAmount;
    
    @Schema(description = "Current status of the installment", example = "OVERDUE")
    private InstallmentStatus status;

    public OverdueInstallmentResponse() {
    }

    public OverdueInstallmentResponse(Integer installmentNumber, LocalDate dueDate, Integer daysOverdue, BigDecimal outstandingPrincipal, BigDecimal penaltyAmount, InstallmentStatus status) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.daysOverdue = daysOverdue;
        this.outstandingPrincipal = outstandingPrincipal;
        this.penaltyAmount = penaltyAmount;
        this.status = status;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public InstallmentStatus getStatus() {
        return status;
    }

    public void setStatus(InstallmentStatus status) {
        this.status = status;
    }
}
