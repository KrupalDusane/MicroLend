package com.microlend.eligibility.dto;

import com.microlend.eligibility.entity.EligibilityDecision;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of the loan eligibility evaluation")
public class EligibilityEvaluationResponse {

    @Schema(description = "Unique evaluation ID", example = "1")
    private Long id;
    
    @Schema(description = "Associated loan application ID", example = "24")
    private Long loanApplicationId;
    
    @Schema(description = "Decision outcome", example = "ELIGIBLE")
    private EligibilityDecision decision;
    
    @Schema(description = "Verified monthly income", example = "5000.00")
    private BigDecimal monthlyIncome;
    
    @Schema(description = "Existing monthly obligations", example = "1000.00")
    private BigDecimal existingMonthlyObligations;
    
    @Schema(description = "Requested loan amount", example = "100000.00")
    private BigDecimal requestedAmount;
    
    @Schema(description = "Loan tenure in months", example = "12")
    private Integer tenureMonths;
    
    @Schema(description = "Estimated Equated Monthly Installment (EMI)", example = "8791.59")
    private BigDecimal estimatedEmi;
    
    @Schema(description = "Calculated Debt-to-Income (DTI) ratio", example = "0.35")
    private BigDecimal debtToIncomeRatio;
    
    @Schema(description = "Calculated Loan-to-Income (LTI) ratio", example = "20.00")
    private BigDecimal loanToIncomeRatio;
    
    @Schema(description = "List of reasons for the decision", example = "[\"DTI is within acceptable limits\"]")
    private List<String> reasons;
    
    @Schema(description = "Timestamp when the evaluation was performed", example = "2026-09-13T10:00:00")
    private LocalDateTime evaluatedAt;

    public EligibilityEvaluationResponse() {
    }

    public EligibilityEvaluationResponse(Long id, Long loanApplicationId, EligibilityDecision decision, BigDecimal monthlyIncome, BigDecimal existingMonthlyObligations, BigDecimal requestedAmount, Integer tenureMonths, BigDecimal estimatedEmi, BigDecimal debtToIncomeRatio, BigDecimal loanToIncomeRatio, List<String> reasons, LocalDateTime evaluatedAt) {
        this.id = id;
        this.loanApplicationId = loanApplicationId;
        this.decision = decision;
        this.monthlyIncome = monthlyIncome;
        this.existingMonthlyObligations = existingMonthlyObligations;
        this.requestedAmount = requestedAmount;
        this.tenureMonths = tenureMonths;
        this.estimatedEmi = estimatedEmi;
        this.debtToIncomeRatio = debtToIncomeRatio;
        this.loanToIncomeRatio = loanToIncomeRatio;
        this.reasons = reasons;
        this.evaluatedAt = evaluatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoanApplicationId() {
        return loanApplicationId;
    }

    public void setLoanApplicationId(Long loanApplicationId) {
        this.loanApplicationId = loanApplicationId;
    }

    public EligibilityDecision getDecision() {
        return decision;
    }

    public void setDecision(EligibilityDecision decision) {
        this.decision = decision;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getExistingMonthlyObligations() {
        return existingMonthlyObligations;
    }

    public void setExistingMonthlyObligations(BigDecimal existingMonthlyObligations) {
        this.existingMonthlyObligations = existingMonthlyObligations;
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

    public BigDecimal getEstimatedEmi() {
        return estimatedEmi;
    }

    public void setEstimatedEmi(BigDecimal estimatedEmi) {
        this.estimatedEmi = estimatedEmi;
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

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
