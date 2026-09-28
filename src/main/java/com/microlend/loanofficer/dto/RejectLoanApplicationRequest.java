package com.microlend.loanofficer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload for rejecting a loan application")
public class RejectLoanApplicationRequest {

    @Schema(description = "Reason for rejection", example = "Applicant's credit score is below the required threshold", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Rejection reason is required")
    @Size(max = 1000, message = "Rejection reason cannot exceed 1000 characters")
    private String reason;

    public RejectLoanApplicationRequest() {
    }

    public RejectLoanApplicationRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
