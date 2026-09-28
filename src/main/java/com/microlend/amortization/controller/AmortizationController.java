package com.microlend.amortization.controller;

import com.microlend.amortization.dto.AmortizationScheduleResponse;
import com.microlend.amortization.service.AmortizationService;
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
@Tag(name = "Amortization", description = "Endpoints for generating and retrieving loan amortization schedules")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/loans")
public class AmortizationController {

    private final AmortizationService amortizationService;

    public AmortizationController(AmortizationService amortizationService) {
        this.amortizationService = amortizationService;
    }

    @Operation(summary = "Generate amortization schedule", description = "Generates the amortization schedule for an APPROVED loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Amortization schedule generated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan is not APPROVED or already has a schedule")
    })
    @PostMapping("/{applicationId}/amortization")
    public ResponseEntity<AmortizationScheduleResponse> generateSchedule(@PathVariable Long applicationId, Principal principal) {
        AmortizationScheduleResponse response = amortizationService.generateSchedule(applicationId, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get amortization schedule", description = "Retrieves the amortization schedule for a loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Amortization schedule retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Amortization schedule not found")
    })
    @GetMapping("/{applicationId}/amortization")
    public ResponseEntity<AmortizationScheduleResponse> getSchedule(@PathVariable Long applicationId, Principal principal) {
        AmortizationScheduleResponse response = amortizationService.getSchedule(applicationId, principal.getName());
        return ResponseEntity.ok(response);
    }
}
