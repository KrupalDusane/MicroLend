package com.microlend.overdue.controller;

import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.overdue.dto.OverdueInstallmentResponse;
import com.microlend.overdue.dto.OverdueProcessingResponse;
import com.microlend.overdue.entity.LoanPenalty;
import com.microlend.overdue.repository.LoanPenaltyRepository;
import com.microlend.overdue.service.OverdueService;
import com.microlend.repayment.repository.RepaymentAllocationRepository;
import com.microlend.user.entity.Role;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Overdue", description = "Endpoints for managing and processing overdue loan installments")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api")
public class OverdueController {

    private final OverdueService overdueService;
    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final LoanPenaltyRepository penaltyRepository;

    private final RepaymentAllocationRepository allocationRepository;

    public OverdueController(OverdueService overdueService,
                             LoanApplicationRepository loanApplicationRepository,
                             UserRepository userRepository,
                             LoanPenaltyRepository penaltyRepository,
                             RepaymentAllocationRepository allocationRepository) {
        this.overdueService = overdueService;
        this.loanApplicationRepository = loanApplicationRepository;
        this.userRepository = userRepository;
        this.penaltyRepository = penaltyRepository;
        this.allocationRepository = allocationRepository;
    }

    @Operation(summary = "Process overdue installments", description = "Triggers the daily scheduled job to process all overdue installments and apply penalties.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue processing completed"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/loan-officer/overdue/process")
    public ResponseEntity<OverdueProcessingResponse> processOverdue() {
        return ResponseEntity.ok(overdueService.processOverdueInstallments());
    }

    @Operation(summary = "Get overdue installments (Officer)", description = "Retrieves overdue installment details for a specific loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue installments retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/loan-officer/applications/{applicationId}/overdue")
    public ResponseEntity<List<OverdueInstallmentResponse>> getOverdueForOfficer(@PathVariable Long applicationId) {
        LoanApplication loan = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan not found"));
        return ResponseEntity.ok(getOverdueResponses(loan));
    }

    @Operation(summary = "Get overdue installments (Customer)", description = "Retrieves overdue installment details for a customer's loan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue installments retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Loan application not found")
    })
    @GetMapping("/loans/{applicationId}/overdue")
    public ResponseEntity<List<OverdueInstallmentResponse>> getOverdueForCustomer(@PathVariable Long applicationId, Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LoanApplication loan = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan not found"));

        if (user.getRole() == Role.CUSTOMER) {
            if (!loan.getCustomer().getUser().getId().equals(user.getId())) {
                throw new LoanApplicationNotFoundException("Loan not found");
            }
        }

        return ResponseEntity.ok(getOverdueResponses(loan));
    }

    private List<OverdueInstallmentResponse> getOverdueResponses(LoanApplication loan) {
        List<LoanPenalty> penalties = penaltyRepository.findByLoanApplication(loan);
        
        // Group penalties by installment
        return penalties.stream()
                .collect(Collectors.groupingBy(LoanPenalty::getInstallment))
                .entrySet().stream().map(entry -> {
                    var inst = entry.getKey();
                    var instPenalties = entry.getValue();
                    
                    java.math.BigDecimal totalPenalty = instPenalties.stream()
                            .map(LoanPenalty::getPenaltyAmount)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                            
                    int maxDaysOverdue = instPenalties.stream()
                            .mapToInt(LoanPenalty::getDaysOverdue)
                            .max().orElse(0);
                            
                    java.math.BigDecimal principalAllocated = allocationRepository.sumPrincipalAllocatedByInstallment(inst).orElse(java.math.BigDecimal.ZERO);
                    java.math.BigDecimal outstandingPrincipal = inst.getPrincipalAmount().subtract(principalAllocated);

                    return new OverdueInstallmentResponse(
                            inst.getInstallmentNumber(),
                            inst.getDueDate(),
                            maxDaysOverdue,
                            outstandingPrincipal,
                            totalPenalty,
                            inst.getStatus()
                    );
                }).collect(Collectors.toList());
    }
}
