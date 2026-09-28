package com.microlend.prepayment.controller;

import com.microlend.prepayment.dto.CreatePrepaymentRequest;
import com.microlend.prepayment.dto.PrepaymentResponse;
import com.microlend.prepayment.dto.SettlementQuoteResponse;
import com.microlend.prepayment.service.PrepaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Prepayment", description = "Endpoints for handling loan prepayments and foreclosure")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api")
public class PrepaymentController {

    private final PrepaymentService prepaymentService;

    public PrepaymentController(PrepaymentService prepaymentService) {
        this.prepaymentService = prepaymentService;
    }

    @Operation(summary = "Get settlement quote", description = "Generates a quote for full foreclosure of the loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Settlement quote generated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan is not active")
    })
    @GetMapping("/loans/{applicationId}/settlement-quote")
    public ResponseEntity<SettlementQuoteResponse> getSettlementQuote(
            @PathVariable Long applicationId,
            Authentication authentication) {
        SettlementQuoteResponse response = prepaymentService.getSettlementQuote(applicationId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Process prepayment", description = "Records a partial or full prepayment against a loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Prepayment recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid prepayment type or amount"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan is not active")
    })
    @PostMapping("/loan-officer/applications/{applicationId}/prepayments")
    public ResponseEntity<PrepaymentResponse> processPrepayment(
            @PathVariable Long applicationId,
            @Valid @RequestBody CreatePrepaymentRequest request,
            Authentication authentication) {
        PrepaymentResponse response = prepaymentService.processPrepayment(applicationId, request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
