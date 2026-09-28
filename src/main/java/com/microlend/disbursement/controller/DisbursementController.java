package com.microlend.disbursement.controller;

import com.microlend.disbursement.dto.LoanDisbursementResponse;
import com.microlend.disbursement.service.DisbursementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Disbursement", description = "Endpoints for loan disbursement operations")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/loans")
public class DisbursementController {

    private final DisbursementService disbursementService;

    public DisbursementController(DisbursementService disbursementService) {
        this.disbursementService = disbursementService;
    }

    @Operation(summary = "Disburse loan", description = "Disburses an APPROVED loan that has a generated amortization schedule.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan disbursed successfully"),
            @ApiResponse(responseCode = "200", description = "Loan was already disbursed"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan not ready for disbursement")
    })
    @PostMapping("/{applicationId}/disbursement")
    public ResponseEntity<LoanDisbursementResponse> disburseLoan(@PathVariable Long applicationId, Principal principal) {
        try {
            LoanDisbursementResponse response = disbursementService.disburseLoan(applicationId, principal.getName());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LoanDisbursementResponse response = disbursementService.getDisbursement(applicationId);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(summary = "Get loan disbursement details", description = "Retrieves disbursement details for a specific loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disbursement details retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Disbursement not found")
    })
    @GetMapping("/{applicationId}/disbursement")
    public ResponseEntity<LoanDisbursementResponse> getDisbursement(@PathVariable Long applicationId) {
        LoanDisbursementResponse response = disbursementService.getDisbursement(applicationId);
        return ResponseEntity.ok(response);
    }
}
