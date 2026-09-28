package com.microlend.repayment.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of loan repayments and outstanding balances")
public class RepaymentSummaryResponse {

    @Schema(description = "Total payable amount of the loan (Principal + Interest)", example = "105499.08")
    private BigDecimal totalLoanAmount;
    
    @Schema(description = "Total amount paid so far", example = "17583.18")
    private BigDecimal totalPaid;
    
    @Schema(description = "Total amount remaining to be paid", example = "87915.90")
    private BigDecimal totalOutstanding;
    
    @Schema(description = "Number of fully paid installments", example = "2")
    private int paidInstallments;
    
    @Schema(description = "Number of partially paid installments", example = "0")
    private int partiallyPaidInstallments;
    
    @Schema(description = "Number of installments remaining", example = "10")
    private int remainingInstallments;
    
    @Schema(description = "Total penalties accrued/paid", example = "0.00")
    private BigDecimal totalPenalties;

    public RepaymentSummaryResponse() {
    }

    public RepaymentSummaryResponse(BigDecimal totalLoanAmount, BigDecimal totalPaid, BigDecimal totalOutstanding, int paidInstallments, int partiallyPaidInstallments, int remainingInstallments, BigDecimal totalPenalties) {
        this.totalLoanAmount = totalLoanAmount;
        this.totalPaid = totalPaid;
        this.totalOutstanding = totalOutstanding;
        this.paidInstallments = paidInstallments;
        this.partiallyPaidInstallments = partiallyPaidInstallments;
        this.remainingInstallments = remainingInstallments;
        this.totalPenalties = totalPenalties;
    }

    public BigDecimal getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public void setTotalLoanAmount(BigDecimal totalLoanAmount) {
        this.totalLoanAmount = totalLoanAmount;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public int getPaidInstallments() {
        return paidInstallments;
    }

    public void setPaidInstallments(int paidInstallments) {
        this.paidInstallments = paidInstallments;
    }

    public int getPartiallyPaidInstallments() {
        return partiallyPaidInstallments;
    }

    public void setPartiallyPaidInstallments(int partiallyPaidInstallments) {
        this.partiallyPaidInstallments = partiallyPaidInstallments;
    }

    public int getRemainingInstallments() {
        return remainingInstallments;
    }

    public void setRemainingInstallments(int remainingInstallments) {
        this.remainingInstallments = remainingInstallments;
    }

    public BigDecimal getTotalPenalties() {
        return totalPenalties;
    }

    public void setTotalPenalties(BigDecimal totalPenalties) {
        this.totalPenalties = totalPenalties;
    }
}
