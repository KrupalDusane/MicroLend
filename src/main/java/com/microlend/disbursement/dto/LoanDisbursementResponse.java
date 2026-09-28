package com.microlend.disbursement.dto;

import com.microlend.disbursement.entity.DisbursementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a loan disbursement")
public class LoanDisbursementResponse {

    @Schema(description = "Disbursement ID", example = "1")
    private Long id;
    
    @Schema(description = "Unique disbursement reference number", example = "DISB-20260913-000001")
    private String disbursementReference;
    
    @Schema(description = "Associated loan application ID", example = "24")
    private Long applicationId;
    
    @Schema(description = "Associated loan application number", example = "ML-20260913-000024")
    private String applicationNumber;
    
    @Schema(description = "Total disbursed amount", example = "100000.00")
    private BigDecimal disbursedAmount;
    
    @Schema(description = "Timestamp when the loan was disbursed", example = "2026-09-13T10:15:00")
    private LocalDateTime disbursedAt;
    
    @Schema(description = "Status of the disbursement", example = "COMPLETED")
    private DisbursementStatus status;

    public LoanDisbursementResponse() {
    }

    public LoanDisbursementResponse(Long id, String disbursementReference, Long applicationId, String applicationNumber, BigDecimal disbursedAmount, LocalDateTime disbursedAt, DisbursementStatus status) {
        this.id = id;
        this.disbursementReference = disbursementReference;
        this.applicationId = applicationId;
        this.applicationNumber = applicationNumber;
        this.disbursedAmount = disbursedAmount;
        this.disbursedAt = disbursedAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisbursementReference() {
        return disbursementReference;
    }

    public void setDisbursementReference(String disbursementReference) {
        this.disbursementReference = disbursementReference;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public BigDecimal getDisbursedAmount() {
        return disbursedAmount;
    }

    public void setDisbursedAmount(BigDecimal disbursedAmount) {
        this.disbursedAmount = disbursedAmount;
    }

    public LocalDateTime getDisbursedAt() {
        return disbursedAt;
    }

    public void setDisbursedAt(LocalDateTime disbursedAt) {
        this.disbursedAt = disbursedAt;
    }

    public DisbursementStatus getStatus() {
        return status;
    }

    public void setStatus(DisbursementStatus status) {
        this.status = status;
    }
}
