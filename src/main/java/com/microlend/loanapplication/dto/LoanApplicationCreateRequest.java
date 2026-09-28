package com.microlend.loanapplication.dto;

import com.microlend.loanapplication.entity.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for creating a new loan application")
public class LoanApplicationCreateRequest {

    @Schema(description = "Type of loan requested", example = "PERSONAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @Schema(description = "Requested loan amount", example = "50000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.01", message = "Requested amount must be greater than zero")
    private BigDecimal requestedAmount;

    @Schema(description = "Loan tenure in months (between 6 and 60)", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Tenure must be at least 6 months")
    @Max(value = 60, message = "Tenure must not exceed 60 months")
    private Integer tenureMonths;

    @Schema(description = "Purpose of the loan", example = "Home renovation", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Purpose is required")
    @Size(max = 1000, message = "Purpose is too long")
    private String purpose;

    public LoanApplicationCreateRequest() {
    }

    public LoanApplicationCreateRequest(LoanType loanType, BigDecimal requestedAmount, Integer tenureMonths, String purpose) {
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.tenureMonths = tenureMonths;
        this.purpose = purpose;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
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

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
