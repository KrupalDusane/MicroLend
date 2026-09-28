package com.microlend.loanapplication.controller;

import com.microlend.loanapplication.dto.LoanApplicationCreateRequest;
import com.microlend.loanapplication.dto.LoanApplicationResponse;
import com.microlend.loanapplication.dto.LoanApplicationUpdateRequest;
import com.microlend.loanapplication.service.LoanApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@Tag(name = "Loan Applications", description = "Endpoints for managing loan applications")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/loan-applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @Operation(summary = "Create loan application", description = "Creates a new loan application in DRAFT status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan application created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or missing customer profile"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createApplication(
            @Valid @RequestBody LoanApplicationCreateRequest request,
            Principal principal) {
        LoanApplicationResponse response = loanApplicationService.createApplication(request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all my loan applications", description = "Retrieves all loan applications for the authenticated customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan applications retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> getMyApplications(Principal principal) {
        List<LoanApplicationResponse> responses = loanApplicationService.getMyApplications(principal.getName());
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get loan application by ID", description = "Retrieves a specific loan application by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan application retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getApplicationById(
            @PathVariable Long id,
            Principal principal) {
        LoanApplicationResponse response = loanApplicationService.getApplicationById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update draft application", description = "Updates an existing DRAFT loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan application updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan application is not in DRAFT state")
    })
    @PutMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> updateDraftApplication(
            @PathVariable Long id,
            @Valid @RequestBody LoanApplicationUpdateRequest request,
            Principal principal) {
        LoanApplicationResponse response = loanApplicationService.updateDraftApplication(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete draft application", description = "Deletes an existing DRAFT loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Loan application deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan application is not in DRAFT state")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDraftApplication(
            @PathVariable Long id,
            Principal principal) {
        loanApplicationService.deleteDraftApplication(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Submit application", description = "Transitions a DRAFT loan application to SUBMITTED state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan application submitted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan application is not in DRAFT state")
    })
    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplicationResponse> submitApplication(
            @PathVariable Long id,
            Principal principal) {
        LoanApplicationResponse response = loanApplicationService.submitApplication(id, principal.getName());
        return ResponseEntity.ok(response);
    }
}
