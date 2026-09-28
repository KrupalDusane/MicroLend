package com.microlend.loanofficer.service;

import com.microlend.common.exception.InvalidLoanApplicationStateException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.common.exception.LoanNotEligibleForApprovalException;
import com.microlend.eligibility.entity.EligibilityDecision;
import com.microlend.eligibility.entity.EligibilityEvaluation;
import com.microlend.eligibility.repository.EligibilityEvaluationRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.loanofficer.dto.LoanOfficerApplicationResponse;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.notification.service.NotificationService;
import com.microlend.notification.entity.NotificationType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanOfficerService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final EligibilityEvaluationRepository eligibilityEvaluationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LoanOfficerService(LoanApplicationRepository loanApplicationRepository,
                              EligibilityEvaluationRepository eligibilityEvaluationRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.eligibilityEvaluationRepository = eligibilityEvaluationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getAuthenticatedLoanOfficer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }

    public List<LoanOfficerApplicationResponse> getOfficerQueue() {
        List<LoanApplication> applications = loanApplicationRepository.findByStatusInOrderBySubmittedAtAsc(
                List.of(LoanApplicationStatus.SUBMITTED, LoanApplicationStatus.UNDER_REVIEW)
        );
        return applications.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public LoanOfficerApplicationResponse getApplicationDetails(Long id) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));
        return mapToResponse(application);
    }

    @Transactional
    public void startReview(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.SUBMITTED) {
            throw new InvalidLoanApplicationStateException("Application must be in SUBMITTED state to start review.");
        }

        application.setStatus(LoanApplicationStatus.UNDER_REVIEW);
        application.setReviewedBy(getAuthenticatedLoanOfficer());
        application.setReviewedAt(LocalDateTime.now());

        loanApplicationRepository.save(application);
    }

    @Transactional
    public void approveApplication(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.UNDER_REVIEW) {
            throw new InvalidLoanApplicationStateException("Application must be in UNDER_REVIEW state to approve.");
        }

        EligibilityEvaluation evaluation = eligibilityEvaluationRepository.findByLoanApplication(application)
                .orElseThrow(() -> new LoanNotEligibleForApprovalException("No eligibility evaluation exists for this application."));

        if (evaluation.getDecision() != EligibilityDecision.ELIGIBLE) {
            throw new LoanNotEligibleForApprovalException("Loan application is not eligible for approval.");
        }

        application.setStatus(LoanApplicationStatus.APPROVED);
        loanApplicationRepository.save(application);
        
        notificationService.createNotification(
            application.getCustomer().getUser(),
            NotificationType.LOAN_APPROVED,
            "Loan Approved",
            "Congratulations! Your loan application " + application.getApplicationNumber() + " has been approved.",
            "LOAN_APPLICATION",
            application.getId()
        );
    }

    @Transactional
    public void rejectApplication(Long applicationId, String reason) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.UNDER_REVIEW) {
            throw new InvalidLoanApplicationStateException("Application must be in UNDER_REVIEW state to reject.");
        }

        application.setStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(reason);
        loanApplicationRepository.save(application);

        notificationService.createNotification(
            application.getCustomer().getUser(),
            NotificationType.LOAN_REJECTED,
            "Loan Rejected",
            "Your loan application " + application.getApplicationNumber() + " was rejected. Reason: " + application.getRejectionReason(),
            "LOAN_APPLICATION",
            application.getId()
        );
    }

    private LoanOfficerApplicationResponse mapToResponse(LoanApplication application) {
        LoanOfficerApplicationResponse response = new LoanOfficerApplicationResponse();
        response.setId(application.getId());
        response.setApplicationNumber(application.getApplicationNumber());
        response.setLoanType(application.getLoanType());
        response.setRequestedAmount(application.getRequestedAmount());
        response.setTenureMonths(application.getTenureMonths());
        response.setPurpose(application.getPurpose());
        response.setStatus(application.getStatus());
        response.setSubmittedAt(application.getSubmittedAt());

        if (application.getReviewedBy() != null) {
            response.setReviewedByEmail(application.getReviewedBy().getEmail());
            response.setReviewedAt(application.getReviewedAt());
        }
        
        response.setRejectionReason(application.getRejectionReason());

        Optional<EligibilityEvaluation> evaluationOpt = eligibilityEvaluationRepository.findByLoanApplication(application);
        if (evaluationOpt.isPresent()) {
            EligibilityEvaluation evaluation = evaluationOpt.get();
            response.setEligibilityDecision(evaluation.getDecision());
            response.setDebtToIncomeRatio(evaluation.getDebtToIncomeRatio());
            response.setLoanToIncomeRatio(evaluation.getLoanToIncomeRatio());
            response.setEstimatedEmi(evaluation.getEstimatedEmi());
        }

        return response;
    }
}
