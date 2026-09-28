package com.microlend.eligibility.controller;

import com.microlend.eligibility.dto.EligibilityEvaluationResponse;
import com.microlend.eligibility.service.EligibilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Eligibility", description = "Endpoints for evaluating loan application eligibility")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/eligibility")
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @Operation(summary = "Evaluate loan application", description = "Evaluates a SUBMITTED loan application for eligibility.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evaluation successful"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found"),
            @ApiResponse(responseCode = "409", description = "Loan application is not in SUBMITTED state")
    })
    @PostMapping("/applications/{applicationId}/evaluate")
    public ResponseEntity<EligibilityEvaluationResponse> evaluate(@PathVariable Long applicationId) {
        EligibilityEvaluationResponse response = eligibilityService.evaluate(applicationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get application evaluation", description = "Retrieves the eligibility evaluation for a specific loan application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evaluation retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Evaluation not found")
    })
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<EligibilityEvaluationResponse> getEvaluation(@PathVariable Long applicationId) {
        EligibilityEvaluationResponse response = eligibilityService.getEvaluation(applicationId);
        return ResponseEntity.ok(response);
    }
}
