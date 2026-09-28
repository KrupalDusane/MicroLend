package com.microlend.loanofficer.dto;

import com.microlend.eligibility.entity.EligibilityDecision;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.entity.LoanType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed view of a loan application for officer review")
public class LoanOfficerApplicationResponse {

    @Schema(description = "Application ID", example = "24")
    private Long id;
    
    @Schema(description = "Unique application number", example = "ML-20260913-000024")
    private String applicationNumber;
    
    @Schema(description = "Type of loan", example = "PERSONAL")
    private LoanType loanType;
    
    @Schema(description = "Requested loan amount", example = "100000.00")
    private BigDecimal requestedAmount;
    
    @Schema(description = "Requested loan tenure in months", example = "12")
    private Integer tenureMonths;
    
    @Schema(description = "Purpose of the loan", example = "Home Renovation")
    private String purpose;
    
    @Schema(description = "Current application status", example = "SUBMITTED")
    private LoanApplicationStatus status;
    
    @Schema(description = "Timestamp of submission", example = "2026-09-13T10:00:00")
    private LocalDateTime submittedAt;
    
    // Audit fields
    @Schema(description = "Email of the officer reviewing this application", example = "officer@microlend.com")
    private String reviewedByEmail;
    
    @Schema(description = "Timestamp when the application was reviewed", example = "2026-09-13T10:30:00")
    private LocalDateTime reviewedAt;
    
    @Schema(description = "Reason for rejection, if applicable", example = "Insufficient income")
    private String rejectionReason;

    // Eligibility details (if evaluated)
    @Schema(description = "Automated eligibility decision", example = "ELIGIBLE")
    private EligibilityDecision eligibilityDecision;
    
    @Schema(description = "Calculated Debt-to-Income ratio", example = "35.5")
    private BigDecimal debtToIncomeRatio;
    
    @Schema(description = "Calculated Loan-to-Income ratio", example = "1.5")
    private BigDecimal loanToIncomeRatio;
    
    @Schema(description = "Estimated monthly EMI", example = "8791.59")
    private BigDecimal estimatedEmi;

    public LoanOfficerApplicationResponse() {
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

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getReviewedByEmail() {
        return reviewedByEmail;
    }

    public void setReviewedByEmail(String reviewedByEmail) {
        this.reviewedByEmail = reviewedByEmail;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public EligibilityDecision getEligibilityDecision() {
        return eligibilityDecision;
    }

    public void setEligibilityDecision(EligibilityDecision eligibilityDecision) {
        this.eligibilityDecision = eligibilityDecision;
    }

    public BigDecimal getDebtToIncomeRatio() {
        return debtToIncomeRatio;
    }

    public void setDebtToIncomeRatio(BigDecimal debtToIncomeRatio) {
        this.debtToIncomeRatio = debtToIncomeRatio;
    }

    public BigDecimal getLoanToIncomeRatio() {
        return loanToIncomeRatio;
    }

    public void setLoanToIncomeRatio(BigDecimal loanToIncomeRatio) {
        this.loanToIncomeRatio = loanToIncomeRatio;
    }

    public BigDecimal getEstimatedEmi() {
        return estimatedEmi;
    }

    public void setEstimatedEmi(BigDecimal estimatedEmi) {
        this.estimatedEmi = estimatedEmi;
    }
}
