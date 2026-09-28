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
import com.microlend.user.entity.Role;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.customer.entity.Customer;
import com.microlend.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanOfficerServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private EligibilityEvaluationRepository eligibilityEvaluationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LoanOfficerService loanOfficerService;

    private User officerUser;
    private LoanApplication application;

    @BeforeEach
    void setUp() {
        officerUser = new User();
        officerUser.setId(1L);
        officerUser.setEmail("officer@example.com");
        officerUser.setRole(Role.LOAN_OFFICER);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("officer@example.com", null)
        );

        User customerUser = new User();
        customerUser.setId(2L);
        Customer customer = new Customer();
        customer.setId(100L);
        customer.setUser(customerUser);

        application = new LoanApplication();
        application.setId(10L);
        application.setCustomer(customer);
    }

    @Test
    void startReview_SubmitsSuccessfully() {
        application.setStatus(LoanApplicationStatus.SUBMITTED);
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officerUser));
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(application));

        loanOfficerService.startReview(10L);

        assertEquals(LoanApplicationStatus.UNDER_REVIEW, application.getStatus());
        assertEquals(officerUser, application.getReviewedBy());
        assertNotNull(application.getReviewedAt());
        verify(loanApplicationRepository).save(application);
    }

    @Test
    void startReview_FailsIfNotSubmitted() {
        application.setStatus(LoanApplicationStatus.DRAFT);
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(application));

        assertThrows(InvalidLoanApplicationStateException.class, () -> loanOfficerService.startReview(10L));
    }

    @Test
    void approveApplication_SucceedsWhenEligible() {
        application.setStatus(LoanApplicationStatus.UNDER_REVIEW);
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(application));

        EligibilityEvaluation evaluation = new EligibilityEvaluation();
        evaluation.setDecision(EligibilityDecision.ELIGIBLE);
        when(eligibilityEvaluationRepository.findByLoanApplication(application)).thenReturn(Optional.of(evaluation));

        loanOfficerService.approveApplication(10L);

        assertEquals(LoanApplicationStatus.APPROVED, application.getStatus());
        verify(loanApplicationRepository).save(application);
    }

    @Test
    void approveApplication_FailsIfNotUnderReview() {
        application.setStatus(LoanApplicationStatus.SUBMITTED);
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(application));

        assertThrows(InvalidLoanApplicationStateException.class, () -> loanOfficerService.approveApplication(10L));
    }

    @Test
    void approveApplication_FailsIfMissingEligibility() {
        application.setStatus(LoanApplicationStatus.UNDER_REVIEW);
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(application));
        when(eligibilityEvaluationRepository.findByLoanApplication(application)).thenReturn(Optional.empty());

        assertThrows(LoanNotEligibleForApprovalException.class, () -> loanOfficerService.approveApplication(10L));
    }

    @Test
    void approveApplication_FailsIfNotEligible() {
        application.setStatus(LoanApplicationStatus.UNDER_REVIEW);
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(application));

        EligibilityEvaluation evaluation = new EligibilityEvaluation();
        evaluation.setDecision(EligibilityDecision.NOT_ELIGIBLE);
        when(eligibilityEvaluationRepository.findByLoanApplication(application)).thenReturn(Optional.of(evaluation));

        assertThrows(LoanNotEligibleForApprovalException.class, () -> loanOfficerService.approveApplication(10L));
    }

    @Test
    void rejectApplication_Succeeds() {
        application.setStatus(LoanApplicationStatus.UNDER_REVIEW);
        when(loanApplicationRepository.findById(10L)).thenReturn(Optional.of(application));

        loanOfficerService.rejectApplication(10L, "Bad credit history");

        assertEquals(LoanApplicationStatus.REJECTED, application.getStatus());
        assertEquals("Bad credit history", application.getRejectionReason());
        verify(loanApplicationRepository).save(application);
    }
}
