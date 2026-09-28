package com.microlend.overdue.entity;

import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.loanapplication.entity.LoanApplication;
import jakarta.persistence.Column;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_penalties", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"installment_id", "penalty_date"})
})
public class LoanPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id", nullable = false)
    private LoanInstallment installment;

    @Column(name = "penalty_date", nullable = false)
    private LocalDate penaltyDate;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal penaltyRate;

    @Column(nullable = false)
    private Integer daysOverdue;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyStatus status = PenaltyStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public LoanPenalty() {
    }

    public LoanPenalty(LoanApplication loanApplication, LoanInstallment installment, LocalDate penaltyDate, BigDecimal penaltyAmount, BigDecimal penaltyRate, Integer daysOverdue, LocalDateTime calculatedAt) {
        this.loanApplication = loanApplication;
        this.installment = installment;
        this.penaltyDate = penaltyDate;
        this.penaltyAmount = penaltyAmount;
        this.penaltyRate = penaltyRate;
        this.daysOverdue = daysOverdue;
        this.calculatedAt = calculatedAt;
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

    public LoanApplication getLoanApplication() {
        return loanApplication;
    }

    public void setLoanApplication(LoanApplication loanApplication) {
        this.loanApplication = loanApplication;
    }

    public LoanInstallment getInstallment() {
        return installment;
    }

    public void setInstallment(LoanInstallment installment) {
        this.installment = installment;
    }

    public LocalDate getPenaltyDate() {
        return penaltyDate;
    }

    public void setPenaltyDate(LocalDate penaltyDate) {
        this.penaltyDate = penaltyDate;
    }

    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public BigDecimal getPenaltyRate() {
        return penaltyRate;
    }

    public void setPenaltyRate(BigDecimal penaltyRate) {
        this.penaltyRate = penaltyRate;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public PenaltyStatus getStatus() {
        return status;
    }

    public void setStatus(PenaltyStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
