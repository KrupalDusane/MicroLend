package com.microlend.prepayment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prepayment_allocations")
public class PrepaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prepayment_id", nullable = false)
    private LoanPrepayment loanPrepayment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAllocated;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAllocated;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal penaltyAllocated;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    private com.microlend.amortization.entity.LoanInstallment installment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "penalty_id")
    private com.microlend.overdue.entity.LoanPenalty penalty;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PrepaymentAllocation() {
    }

    public PrepaymentAllocation(LoanPrepayment loanPrepayment, com.microlend.amortization.entity.LoanInstallment installment, com.microlend.overdue.entity.LoanPenalty penalty, BigDecimal principalAllocated, BigDecimal interestAllocated, BigDecimal penaltyAllocated) {
        this.loanPrepayment = loanPrepayment;
        this.installment = installment;
        this.penalty = penalty;
        this.principalAllocated = principalAllocated;
        this.interestAllocated = interestAllocated;
        this.penaltyAllocated = penaltyAllocated;
        this.allocatedAmount = principalAllocated.add(interestAllocated).add(penaltyAllocated);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanPrepayment getLoanPrepayment() {
        return loanPrepayment;
    }

    public void setLoanPrepayment(LoanPrepayment loanPrepayment) {
        this.loanPrepayment = loanPrepayment;
    }

    public BigDecimal getPrincipalAllocated() {
        return principalAllocated;
    }

    public void setPrincipalAllocated(BigDecimal principalAllocated) {
        this.principalAllocated = principalAllocated;
    }

    public BigDecimal getInterestAllocated() {
        return interestAllocated;
    }

    public void setInterestAllocated(BigDecimal interestAllocated) {
        this.interestAllocated = interestAllocated;
    }

    public BigDecimal getPenaltyAllocated() {
        return penaltyAllocated;
    }

    public void setPenaltyAllocated(BigDecimal penaltyAllocated) {
        this.penaltyAllocated = penaltyAllocated;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public com.microlend.amortization.entity.LoanInstallment getInstallment() {
        return installment;
    }

    public void setInstallment(com.microlend.amortization.entity.LoanInstallment installment) {
        this.installment = installment;
    }

    public com.microlend.overdue.entity.LoanPenalty getPenalty() {
        return penalty;
    }

    public void setPenalty(com.microlend.overdue.entity.LoanPenalty penalty) {
        this.penalty = penalty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
