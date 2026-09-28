package com.microlend.repayment.dto;

import com.microlend.repayment.entity.RepaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a recorded loan repayment")
public class LoanRepaymentResponse {

    @Schema(description = "Unique transaction reference", example = "REP-20260913-000001")
    private String transactionReference;
    
    @Schema(description = "Total amount paid", example = "8791.59")
    private BigDecimal amount;
    
    @Schema(description = "Timestamp of payment", example = "2026-09-13T10:15:00")
    private LocalDateTime paidAt;
    
    @Schema(description = "Status of the repayment", example = "ALLOCATED")
    private RepaymentStatus status;
    
    @Schema(description = "User who recorded the repayment", example = "john.doe")
    private String recordedBy;
    
    @Schema(description = "Detailed breakdown of how the repayment was allocated")
    private List<RepaymentAllocationResponse> allocations;

    public LoanRepaymentResponse() {
    }

    public LoanRepaymentResponse(String transactionReference, BigDecimal amount, LocalDateTime paidAt, RepaymentStatus status, String recordedBy, List<RepaymentAllocationResponse> allocations) {
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.paidAt = paidAt;
        this.status = status;
        this.recordedBy = recordedBy;
        this.allocations = allocations;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public RepaymentStatus getStatus() {
        return status;
    }

    public void setStatus(RepaymentStatus status) {
        this.status = status;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public List<RepaymentAllocationResponse> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<RepaymentAllocationResponse> allocations) {
        this.allocations = allocations;
    }
}
