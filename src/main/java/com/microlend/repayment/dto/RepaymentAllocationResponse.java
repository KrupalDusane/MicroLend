package com.microlend.repayment.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of how a repayment was allocated to a specific installment")
public class RepaymentAllocationResponse {

    @Schema(description = "Installment sequence number", example = "1")
    private Integer installmentNumber;
    
    @Schema(description = "Amount allocated towards interest", example = "833.33")
    private BigDecimal interestAllocated;
    
    @Schema(description = "Amount allocated towards principal", example = "7958.26")
    private BigDecimal principalAllocated;
    
    @Schema(description = "Total amount allocated to this installment", example = "8791.59")
    private BigDecimal totalAllocated;

    public RepaymentAllocationResponse() {
    }

    public RepaymentAllocationResponse(Integer installmentNumber, BigDecimal interestAllocated, BigDecimal principalAllocated, BigDecimal totalAllocated) {
        this.installmentNumber = installmentNumber;
        this.interestAllocated = interestAllocated;
        this.principalAllocated = principalAllocated;
        this.totalAllocated = totalAllocated;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getInterestAllocated() {
        return interestAllocated;
    }

    public void setInterestAllocated(BigDecimal interestAllocated) {
        this.interestAllocated = interestAllocated;
    }

    public BigDecimal getPrincipalAllocated() {
        return principalAllocated;
    }

    public void setPrincipalAllocated(BigDecimal principalAllocated) {
        this.principalAllocated = principalAllocated;
    }

    public BigDecimal getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(BigDecimal totalAllocated) {
        this.totalAllocated = totalAllocated;
    }
}
