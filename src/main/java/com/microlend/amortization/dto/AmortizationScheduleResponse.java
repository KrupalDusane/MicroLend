package com.microlend.amortization.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Loan amortization schedule overview")
public class AmortizationScheduleResponse {

    @Schema(description = "Loan application ID", example = "24")
    private Long applicationId;
    
    @Schema(description = "Loan application number", example = "ML-20260913-000024")
    private String applicationNumber;
    
    @Schema(description = "Principal amount", example = "100000.00")
    private BigDecimal principal;
    
    @Schema(description = "Annual interest rate applied", example = "10.00")
    private BigDecimal annualInterestRate;
    
    @Schema(description = "Monthly EMI amount", example = "8791.59")
    private BigDecimal monthlyEmi;
    
    @Schema(description = "Total number of installments", example = "12")
    private Integer numberOfInstallments;
    
    @Schema(description = "Total principal to be repaid", example = "100000.00")
    private BigDecimal totalPrincipal;
    
    @Schema(description = "Total interest to be paid", example = "5499.08")
    private BigDecimal totalInterest;
    
    @Schema(description = "Total payable amount", example = "105499.08")
    private BigDecimal totalPayable;
    
    @Schema(description = "Timestamp when the schedule was created", example = "2026-09-13T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "List of scheduled installments")
    private List<LoanInstallmentResponse> installments;

    public AmortizationScheduleResponse() {
    }

    public AmortizationScheduleResponse(Long applicationId, String applicationNumber, BigDecimal principal, BigDecimal annualInterestRate, BigDecimal monthlyEmi, Integer numberOfInstallments, BigDecimal totalPrincipal, BigDecimal totalInterest, BigDecimal totalPayable, LocalDateTime createdAt, List<LoanInstallmentResponse> installments) {
        this.applicationId = applicationId;
        this.applicationNumber = applicationNumber;
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.monthlyEmi = monthlyEmi;
        this.numberOfInstallments = numberOfInstallments;
        this.totalPrincipal = totalPrincipal;
        this.totalInterest = totalInterest;
        this.totalPayable = totalPayable;
        this.createdAt = createdAt;
        this.installments = installments;
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

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public BigDecimal getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(BigDecimal monthlyEmi) {
        this.monthlyEmi = monthlyEmi;
    }

    public Integer getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(Integer numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public BigDecimal getTotalPrincipal() {
        return totalPrincipal;
    }

    public void setTotalPrincipal(BigDecimal totalPrincipal) {
        this.totalPrincipal = totalPrincipal;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(BigDecimal totalPayable) {
        this.totalPayable = totalPayable;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LoanInstallmentResponse> getInstallments() {
        return installments;
    }

    public void setInstallments(List<LoanInstallmentResponse> installments) {
        this.installments = installments;
    }
}
