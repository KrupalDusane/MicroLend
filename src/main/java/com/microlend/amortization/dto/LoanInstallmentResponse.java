package com.microlend.amortization.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of a single loan installment")
public class LoanInstallmentResponse {

    @Schema(description = "Installment sequence number", example = "1")
    private Integer installmentNumber;
    
    @Schema(description = "Due date for this installment", example = "2026-10-13")
    private LocalDate dueDate;
    
    @Schema(description = "Opening balance before this installment", example = "100000.00")
    private BigDecimal openingBalance;
    
    @Schema(description = "Total EMI amount for this installment", example = "8791.59")
    private BigDecimal emiAmount;
    
    @Schema(description = "Principal component of the EMI", example = "7958.26")
    private BigDecimal principalAmount;
    
    @Schema(description = "Interest component of the EMI", example = "833.33")
    private BigDecimal interestAmount;
    
    @Schema(description = "Closing balance after this installment", example = "92041.74")
    private BigDecimal closingBalance;

    public LoanInstallmentResponse() {
    }

    public LoanInstallmentResponse(Integer installmentNumber, LocalDate dueDate, BigDecimal openingBalance, BigDecimal emiAmount, BigDecimal principalAmount, BigDecimal interestAmount, BigDecimal closingBalance) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.openingBalance = openingBalance;
        this.emiAmount = emiAmount;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.closingBalance = closingBalance;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(BigDecimal emiAmount) {
        this.emiAmount = emiAmount;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public BigDecimal getClosingBalance() {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance) {
        this.closingBalance = closingBalance;
    }
}
