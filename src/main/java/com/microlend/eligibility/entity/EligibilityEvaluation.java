package com.microlend.eligibility.entity;

import com.microlend.loanapplication.entity.LoanApplication;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eligibility_evaluations", uniqueConstraints = {
        @UniqueConstraint(columnNames = "loan_application_id")
})
public class EligibilityEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EligibilityDecision decision;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal existingMonthlyObligations;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedAmount;

    @Column(nullable = false)
    private Integer tenureMonths;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal estimatedEmi;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal debtToIncomeRatio;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal loanToIncomeRatio;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "eligibility_evaluation_reasons", joinColumns = @JoinColumn(name = "eligibility_evaluation_id"))
    @Column(name = "reason", length = 500)
    private List<String> reasons = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;

    public EligibilityEvaluation() {
    }

    public EligibilityEvaluation(LoanApplication loanApplication, EligibilityDecision decision, BigDecimal monthlyIncome, BigDecimal existingMonthlyObligations, BigDecimal requestedAmount, Integer tenureMonths, BigDecimal estimatedEmi, BigDecimal debtToIncomeRatio, BigDecimal loanToIncomeRatio, List<String> reasons) {
        this.loanApplication = loanApplication;
        this.decision = decision;
        this.monthlyIncome = monthlyIncome;
        this.existingMonthlyObligations = existingMonthlyObligations;
        this.requestedAmount = requestedAmount;
        this.tenureMonths = tenureMonths;
        this.estimatedEmi = estimatedEmi;
        this.debtToIncomeRatio = debtToIncomeRatio;
        this.loanToIncomeRatio = loanToIncomeRatio;
        this.reasons = reasons != null ? reasons : new ArrayList<>();
        this.evaluatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onEvaluate() {
        this.evaluatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplication loanApplication) {
        this.loanApplication = loanApplication;
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
