package com.microlend.eligibility.service;

import com.microlend.eligibility.entity.EligibilityDecision;

import java.math.BigDecimal;
import java.util.List;

public class EligibilityCalculationResult {

    private final EligibilityDecision decision;
    private final BigDecimal estimatedEmi;
    private final BigDecimal debtToIncomeRatio;
    private final BigDecimal loanToIncomeRatio;
    private final List<String> reasons;

    public EligibilityCalculationResult(EligibilityDecision decision, BigDecimal estimatedEmi, BigDecimal debtToIncomeRatio, BigDecimal loanToIncomeRatio, List<String> reasons) {
        this.decision = decision;
        this.estimatedEmi = estimatedEmi;
        this.debtToIncomeRatio = debtToIncomeRatio;
        this.loanToIncomeRatio = loanToIncomeRatio;
        this.reasons = reasons;
    }

    public EligibilityDecision getDecision() {
        return decision;
    }

    public BigDecimal getEstimatedEmi() {
        return estimatedEmi;
    }

    public BigDecimal getDebtToIncomeRatio() {
        return debtToIncomeRatio;
    }

    public BigDecimal getLoanToIncomeRatio() {
        return loanToIncomeRatio;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
