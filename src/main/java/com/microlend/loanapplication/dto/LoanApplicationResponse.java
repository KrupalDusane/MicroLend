package com.microlend.loanapplication.dto;

import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.entity.LoanType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Loan application information")
public class LoanApplicationResponse {

    @Schema(description = "Unique application ID", example = "1")
    private Long id;
    
    @Schema(description = "Generated application number", example = "ML-20260913-000024")
    private String applicationNumber;
    
    @Schema(description = "Type of loan requested", example = "PERSONAL")
    private LoanType loanType;
    
    @Schema(description = "Requested loan amount", example = "50000.00")
    private BigDecimal requestedAmount;
    
    @Schema(description = "Loan tenure in months", example = "12")
    private Integer tenureMonths;
    
    @Schema(description = "Purpose of the loan", example = "Home renovation")
    private String purpose;
    
    @Schema(description = "Current application status", example = "DRAFT")
    private LoanApplicationStatus status;
    
    @Schema(description = "Timestamp when the application was created", example = "2026-09-13T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the application was last updated", example = "2026-09-13T10:00:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Timestamp when the application was submitted (null if DRAFT)", example = "2026-09-13T10:05:00")
    private LocalDateTime submittedAt;

    public LoanApplicationResponse() {
    }

    public LoanApplicationResponse(Long id, String applicationNumber, LoanType loanType, BigDecimal requestedAmount, Integer tenureMonths, String purpose, LoanApplicationStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime submittedAt) {
        this.id = id;
        this.applicationNumber = applicationNumber;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.tenureMonths = tenureMonths;
        this.purpose = purpose;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LoanApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(LoanApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
