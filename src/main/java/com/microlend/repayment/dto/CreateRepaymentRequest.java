package com.microlend.repayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for recording a loan repayment")
public class CreateRepaymentRequest {

    @Schema(description = "Repayment amount", example = "8791.59", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Repayment amount is required")
    @DecimalMin(value = "0.01", message = "Repayment amount must be greater than zero")
    private BigDecimal amount;

    public CreateRepaymentRequest() {
    }

    public CreateRepaymentRequest(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
