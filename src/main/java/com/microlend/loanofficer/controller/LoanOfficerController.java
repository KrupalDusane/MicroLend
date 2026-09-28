package com.microlend.loanofficer.controller;

import com.microlend.loanofficer.dto.LoanOfficerApplicationResponse;
import com.microlend.loanofficer.dto.RejectLoanApplicationRequest;
import com.microlend.loanofficer.service.LoanOfficerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Loan Officer", description = "Endpoints for loan officers to manage and review applications")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/loan-officer/applications")
public class LoanOfficerController {
    private final LoanOfficerService loanOfficerService;

    public LoanOfficerController(LoanOfficerService loanOfficerService) {
        this.loanOfficerService = loanOfficerService;
    }

    @Operation(summary = "Get officer queue", description = "Retrieves all loan applications that are ready for review or currently in review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<List<LoanOfficerApplicationResponse>> getOfficerQueue() {
        return ResponseEntity.ok(loanOfficerService.getOfficerQueue());
    }

    @Operation(summary = "Get application details", description = "Retrieves the full details of a loan application for officer review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application details retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanOfficerApplicationResponse> getApplicationDetails(@PathVariable Long id) {
        return ResponseEntity.ok(loanOfficerService.getApplicationDetails(id));
    }

    @Operation(summary = "Start application review", description = "Transitions a SUBMITTED application to IN_REVIEW status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review started"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan is not in SUBMITTED state")
    })
    @PostMapping("/{id}/review")
    public ResponseEntity<Void> startReview(@PathVariable Long id) {
        loanOfficerService.startReview(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Approve application", description = "Approves a loan application that is currently IN_REVIEW.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application approved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan is not IN_REVIEW")
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveApplication(@PathVariable Long id) {
        loanOfficerService.approveApplication(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reject application", description = "Rejects a loan application that is currently IN_REVIEW, with a reason.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid rejection reason"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan is not IN_REVIEW")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable Long id, @Valid @RequestBody RejectLoanApplicationRequest request) {
        loanOfficerService.rejectApplication(id, request.getReason());
        return ResponseEntity.ok().build();
    }
}
