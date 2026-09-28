package com.microlend.disbursement.entity;

import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.user.entity.User;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_disbursements", uniqueConstraints = {
        @UniqueConstraint(columnNames = "loan_application_id")
})
public class LoanDisbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(nullable = false)
    private LocalDateTime disbursedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursed_by_id", nullable = false)
    private User disbursedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisbursementStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(unique = true)
    private String disbursementReference;

    public LoanDisbursement() {
    }

    public LoanDisbursement(LoanApplication loanApplication, BigDecimal disbursedAmount, User disbursedBy, DisbursementStatus status) {
        this.loanApplication = loanApplication;
        this.disbursedAmount = disbursedAmount;
        this.disbursedBy = disbursedBy;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.disbursedAt = this.createdAt;
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

    public User getDisbursedBy() {
        return disbursedBy;
    }

    public void setDisbursedBy(User disbursedBy) {
        this.disbursedBy = disbursedBy;
    }

    public DisbursementStatus getStatus() {
        return status;
    }

    public void setStatus(DisbursementStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDisbursementReference() {
        return disbursementReference;
    }

    public void setDisbursementReference(String disbursementReference) {
        this.disbursementReference = disbursementReference;
    }
}
