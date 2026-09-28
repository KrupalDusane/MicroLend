package com.microlend.amortization.service;

import com.microlend.amortization.dto.AmortizationScheduleResponse;
import com.microlend.amortization.dto.LoanInstallmentResponse;
import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.repository.LoanAmortizationScheduleRepository;
import com.microlend.common.exception.CustomerNotFoundException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.common.exception.LoanNotApprovedException;
import com.microlend.customer.entity.Customer;
import com.microlend.customer.repository.CustomerRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class AmortizationService {

    private final LoanAmortizationScheduleRepository scheduleRepository;
    private final AmortizationCalculator amortizationCalculator;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public AmortizationService(LoanAmortizationScheduleRepository scheduleRepository, AmortizationCalculator amortizationCalculator, LoanApplicationRepository loanApplicationRepository, CustomerRepository customerRepository, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.amortizationCalculator = amortizationCalculator;
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AmortizationScheduleResponse generateSchedule(Long applicationId, String authenticatedEmail) {
        Customer customer = getAuthenticatedCustomer(authenticatedEmail);

        LoanApplication loanApplication = loanApplicationRepository.findByIdAndCustomer(applicationId, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found or does not belong to the authenticated customer."));

        if (loanApplication.getStatus() != LoanApplicationStatus.APPROVED) {
            throw new LoanNotApprovedException("Cannot generate amortization schedule for a non-approved loan application.");
        }

        return scheduleRepository.findByLoanApplication(loanApplication)
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    LoanAmortizationSchedule newSchedule = amortizationCalculator.generateSchedule(loanApplication, LocalDate.now());
                    newSchedule = scheduleRepository.save(newSchedule);
                    return mapToResponse(newSchedule);
                });
    }

    @Transactional(readOnly = true)
    public AmortizationScheduleResponse getSchedule(Long applicationId, String authenticatedEmail) {
        Customer customer = getAuthenticatedCustomer(authenticatedEmail);

        LoanApplication loanApplication = loanApplicationRepository.findByIdAndCustomer(applicationId, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found or does not belong to the authenticated customer."));

        LoanAmortizationSchedule schedule = scheduleRepository.findByLoanApplication(loanApplication)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Amortization schedule not found for this loan application."));

        return mapToResponse(schedule);
    }

    private Customer getAuthenticatedCustomer(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        return customerRepository.findByUser(user)
                .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found. Please create a customer profile first."));
    }

    private AmortizationScheduleResponse mapToResponse(LoanAmortizationSchedule schedule) {
        return new AmortizationScheduleResponse(
                schedule.getLoanApplication().getId(),
                schedule.getLoanApplication().getApplicationNumber(),
                schedule.getLoanApplication().getRequestedAmount(),
                schedule.getAnnualInterestRate(),
                schedule.getMonthlyEmi(),
                schedule.getNumberOfInstallments(),
                schedule.getTotalPrincipal(),
                schedule.getTotalInterest(),
                schedule.getTotalPayable(),
                schedule.getCreatedAt(),
                schedule.getInstallments().stream().map(i -> new LoanInstallmentResponse(
                        i.getInstallmentNumber(),
                        i.getDueDate(),
                        i.getOpeningBalance(),
                        i.getEmiAmount(),
                        i.getPrincipalAmount(),
                        i.getInterestAmount(),
                        i.getClosingBalance()
                )).collect(Collectors.toList())
        );
    }
}
