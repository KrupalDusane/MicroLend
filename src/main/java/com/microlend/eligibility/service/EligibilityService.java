package com.microlend.eligibility.service;

import com.microlend.common.exception.CustomerNotFoundException;
import com.microlend.common.exception.EligibilityEvaluationNotFoundException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.common.exception.LoanApplicationNotSubmittedException;
import com.microlend.customer.entity.Customer;
import com.microlend.customer.repository.CustomerRepository;
import com.microlend.eligibility.dto.EligibilityEvaluationResponse;
import com.microlend.eligibility.entity.EligibilityEvaluation;
import com.microlend.eligibility.repository.EligibilityEvaluationRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.notification.service.NotificationService;
import com.microlend.notification.entity.NotificationType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EligibilityService {

    private final EligibilityEvaluationRepository eligibilityEvaluationRepository;
    private final EligibilityCalculator eligibilityCalculator;
    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public EligibilityService(EligibilityEvaluationRepository eligibilityEvaluationRepository, EligibilityCalculator eligibilityCalculator, LoanApplicationRepository loanApplicationRepository, CustomerRepository customerRepository, UserRepository userRepository, NotificationService notificationService) {
        this.eligibilityEvaluationRepository = eligibilityEvaluationRepository;
        this.eligibilityCalculator = eligibilityCalculator;
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public EligibilityEvaluationResponse evaluate(Long applicationId) {
        Customer customer = getAuthenticatedCustomer();

        LoanApplication loanApplication = loanApplicationRepository.findByIdAndCustomer(applicationId, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found or does not belong to the authenticated customer."));

        if (loanApplication.getStatus() != LoanApplicationStatus.SUBMITTED) {
            throw new LoanApplicationNotSubmittedException("Cannot evaluate eligibility for a " + loanApplication.getStatus() + " loan application.");
        }

        EligibilityCalculationResult calcResult = eligibilityCalculator.calculate(
                customer.getMonthlyIncome(),
                customer.getExistingMonthlyObligations(),
                loanApplication.getRequestedAmount(),
                loanApplication.getTenureMonths()
        );

        Optional<EligibilityEvaluation> existingEvaluationOpt = eligibilityEvaluationRepository.findByLoanApplication(loanApplication);
        
        EligibilityEvaluation evaluation;
        if (existingEvaluationOpt.isPresent()) {
            evaluation = existingEvaluationOpt.get();
            evaluation.setDecision(calcResult.getDecision());
            evaluation.setMonthlyIncome(customer.getMonthlyIncome());
            evaluation.setExistingMonthlyObligations(customer.getExistingMonthlyObligations());
            evaluation.setRequestedAmount(loanApplication.getRequestedAmount());
            evaluation.setTenureMonths(loanApplication.getTenureMonths());
            evaluation.setEstimatedEmi(calcResult.getEstimatedEmi());
            evaluation.setDebtToIncomeRatio(calcResult.getDebtToIncomeRatio());
            evaluation.setLoanToIncomeRatio(calcResult.getLoanToIncomeRatio());
            evaluation.getReasons().clear();
            evaluation.getReasons().addAll(calcResult.getReasons());
        } else {
            evaluation = new EligibilityEvaluation(
                    loanApplication,
                    calcResult.getDecision(),
                    customer.getMonthlyIncome(),
                    customer.getExistingMonthlyObligations(),
                    loanApplication.getRequestedAmount(),
                    loanApplication.getTenureMonths(),
                    calcResult.getEstimatedEmi(),
                    calcResult.getDebtToIncomeRatio(),
                    calcResult.getLoanToIncomeRatio(),
                    calcResult.getReasons()
            );
        }

        evaluation = eligibilityEvaluationRepository.save(evaluation);

        notificationService.createNotification(
            customer.getUser(),
            NotificationType.ELIGIBILITY_EVALUATED,
            "Eligibility Evaluated",
            "Your loan eligibility evaluation is complete. Decision: " + evaluation.getDecision() + ".",
            "LOAN_APPLICATION",
            loanApplication.getId()
        );

        return mapToResponse(evaluation);
    }

    @Transactional(readOnly = true)
    public EligibilityEvaluationResponse getEvaluation(Long applicationId) {
        Customer customer = getAuthenticatedCustomer();

        LoanApplication loanApplication = loanApplicationRepository.findByIdAndCustomer(applicationId, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found or does not belong to the authenticated customer."));

        EligibilityEvaluation evaluation = eligibilityEvaluationRepository.findByLoanApplication(loanApplication)
                .orElseThrow(() -> new EligibilityEvaluationNotFoundException("No eligibility evaluation found for this loan application."));

        return mapToResponse(evaluation);
    }

    private Customer getAuthenticatedCustomer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

        return customerRepository.findByUser(user)
                .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found. Please create a customer profile first."));
    }

    private EligibilityEvaluationResponse mapToResponse(EligibilityEvaluation evaluation) {
        return new EligibilityEvaluationResponse(
                evaluation.getId(),
                evaluation.getLoanApplication().getId(),
                evaluation.getDecision(),
                evaluation.getMonthlyIncome(),
                evaluation.getExistingMonthlyObligations(),
                evaluation.getRequestedAmount(),
                evaluation.getTenureMonths(),
                evaluation.getEstimatedEmi(),
                evaluation.getDebtToIncomeRatio(),
                evaluation.getLoanToIncomeRatio(),
                evaluation.getReasons(),
                evaluation.getEvaluatedAt()
        );
    }
}
