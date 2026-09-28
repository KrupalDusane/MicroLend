package com.microlend.prepayment.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details of how a prepayment was allocated")
public class PrepaymentAllocationResponse {

    @Schema(description = "Amount allocated to outstanding principal", example = "50000.00")
    private BigDecimal principalAllocated;
    
    @Schema(description = "Amount allocated to outstanding interest", example = "0.00")
    private BigDecimal interestAllocated;
    
    @Schema(description = "Amount allocated to penalties", example = "0.00")
    private BigDecimal penaltyAllocated;
    
    @Schema(description = "Total amount allocated", example = "50000.00")
    private BigDecimal allocatedAmount;

    public PrepaymentAllocationResponse() {
    }

    public PrepaymentAllocationResponse(BigDecimal principalAllocated, BigDecimal interestAllocated, BigDecimal penaltyAllocated, BigDecimal allocatedAmount) {
        this.principalAllocated = principalAllocated;
        this.interestAllocated = interestAllocated;
        this.penaltyAllocated = penaltyAllocated;
        this.allocatedAmount = allocatedAmount;
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
}
