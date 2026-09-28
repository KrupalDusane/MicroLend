package com.microlend.repayment.entity;

import com.microlend.amortization.entity.LoanInstallment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "repayment_allocations")
public class RepaymentAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_id", nullable = false)
    private LoanRepayment repayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id", nullable = false)
    private LoanInstallment installment;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal interestAllocated;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAllocated;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public RepaymentAllocation() {
    }

    public RepaymentAllocation(LoanRepayment repayment, LoanInstallment installment, BigDecimal interestAllocated, BigDecimal principalAllocated) {
        this.repayment = repayment;
        this.installment = installment;
        this.interestAllocated = interestAllocated;
        this.principalAllocated = principalAllocated;
        this.allocatedAmount = interestAllocated.add(principalAllocated);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanRepayment getRepayment() {
        return repayment;
    }

    public void setRepayment(LoanRepayment repayment) {
        this.repayment = repayment;
    }

    public LoanInstallment getInstallment() {
        return installment;
    }

    public void setInstallment(LoanInstallment installment) {
        this.installment = installment;
    }

    public BigDecimal getInterestAllocated() {
        return interestAllocated;
    }

    public void setInterestAllocated(BigDecimal interestAllocated) {
        this.interestAllocated = interestAllocated;
        if (this.principalAllocated != null) {
            this.allocatedAmount = this.interestAllocated.add(this.principalAllocated);
        }
    }

    public BigDecimal getPrincipalAllocated() {
        return principalAllocated;
    }

    public void setPrincipalAllocated(BigDecimal principalAllocated) {
        this.principalAllocated = principalAllocated;
        if (this.interestAllocated != null) {
            this.allocatedAmount = this.interestAllocated.add(this.principalAllocated);
        }
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
