package com.microlend.repayment.controller;

import com.microlend.repayment.dto.CreateRepaymentRequest;
import com.microlend.repayment.dto.LoanRepaymentResponse;
import com.microlend.repayment.dto.RepaymentSummaryResponse;
import com.microlend.repayment.service.RepaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Repayment", description = "Endpoints for managing loan repayments")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/loans")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(RepaymentService repaymentService) {
        this.repaymentService = repaymentService;
    }

    @Operation(summary = "Record repayment", description = "Records a new repayment against a disbursed loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Repayment recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid repayment amount"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan is not in REPAYING state")
    })
    @PostMapping("/{applicationId}/repayments")
    public ResponseEntity<LoanRepaymentResponse> recordRepayment(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreateRepaymentRequest request,
            Principal principal) {
        LoanRepaymentResponse response = repaymentService.recordRepayment(applicationId, request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get repayment history", description = "Retrieves the repayment history for a specific loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repayment history retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/{applicationId}/repayments")
    public ResponseEntity<List<LoanRepaymentResponse>> getRepaymentHistory(@PathVariable Long applicationId, Principal principal) {
        List<LoanRepaymentResponse> history = repaymentService.getRepaymentHistory(applicationId, principal.getName());
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Get repayment summary", description = "Retrieves the current repayment summary and outstanding balance for a loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Repayment summary retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/{applicationId}/repayment-summary")
    public ResponseEntity<RepaymentSummaryResponse> getRepaymentSummary(@PathVariable Long applicationId, Principal principal) {
        RepaymentSummaryResponse summary = repaymentService.getRepaymentSummary(applicationId, principal.getName());
        return ResponseEntity.ok(summary);
    }
}
