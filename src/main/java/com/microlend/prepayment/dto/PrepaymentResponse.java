package com.microlend.prepayment.dto;

import com.microlend.prepayment.entity.PrepaymentType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a recorded loan prepayment")
public class PrepaymentResponse {

    @Schema(description = "Unique transaction reference", example = "PRE-20260913-000001")
    private String transactionReference;
    
    @Schema(description = "Total amount prepaid", example = "50000.00")
    private BigDecimal amount;
    
    @Schema(description = "Type of prepayment", example = "PARTIAL")
    private PrepaymentType prepaymentType;
    
    @Schema(description = "Timestamp of prepayment", example = "2026-09-13T10:15:00")
    private LocalDateTime paidAt;
    
    @Schema(description = "User who recorded the prepayment", example = "officer.jane")
    private String recordedBy;
    
    @Schema(description = "Allocation details of the prepayment")
    private PrepaymentAllocationResponse allocation;

    public PrepaymentResponse() {
    }

    public PrepaymentResponse(String transactionReference, BigDecimal amount, PrepaymentType prepaymentType, LocalDateTime paidAt, String recordedBy, PrepaymentAllocationResponse allocation) {
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.prepaymentType = prepaymentType;
        this.paidAt = paidAt;
        this.recordedBy = recordedBy;
        this.allocation = allocation;
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

    public PrepaymentType getPrepaymentType() {
        return prepaymentType;
    }

    public void setPrepaymentType(PrepaymentType prepaymentType) {
        this.prepaymentType = prepaymentType;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(String recordedBy) {
        this.recordedBy = recordedBy;
    }

    public PrepaymentAllocationResponse getAllocation() {
        return allocation;
    }

    public void setAllocation(PrepaymentAllocationResponse allocation) {
        this.allocation = allocation;
    }
}
