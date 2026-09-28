package com.microlend.prepayment.dto;

import com.microlend.prepayment.entity.PrepaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for recording a loan prepayment")
public class CreatePrepaymentRequest {

    @Schema(description = "Amount being prepaid", example = "50000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1")
    private BigDecimal amount;

    @Schema(description = "Type of prepayment (PARTIAL or FULL_FORECLOSURE)", example = "PARTIAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Prepayment type is required")
    private PrepaymentType prepaymentType;

    public CreatePrepaymentRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PrepaymentType getPrepaymentType() {
        return prepaymentType;
    }

    public void setPrepaymentType(PrepaymentType prepaymentType) {
        this.prepaymentType = prepaymentType;
    }
}
