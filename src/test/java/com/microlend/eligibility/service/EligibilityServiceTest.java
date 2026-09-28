package com.microlend.eligibility.service;

import com.microlend.common.exception.CustomerNotFoundException;
import com.microlend.common.exception.EligibilityEvaluationNotFoundException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.common.exception.LoanApplicationNotSubmittedException;
import com.microlend.customer.entity.Customer;
import com.microlend.customer.repository.CustomerRepository;
import com.microlend.eligibility.dto.EligibilityEvaluationResponse;
import com.microlend.eligibility.entity.EligibilityDecision;
import com.microlend.eligibility.entity.EligibilityEvaluation;
import com.microlend.eligibility.repository.EligibilityEvaluationRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {

    @Mock
    private EligibilityEvaluationRepository eligibilityEvaluationRepository;

    @Mock
    private EligibilityCalculator eligibilityCalculator;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EligibilityService eligibilityService;

    private User mockUser;
    private Customer mockCustomer;
    private LoanApplication mockApplication;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@example.com");

        mockCustomer = new Customer();
        mockCustomer.setId(1L);
        mockCustomer.setMonthlyIncome(new BigDecimal("50000.00"));
        mockCustomer.setExistingMonthlyObligations(new BigDecimal("10000.00"));

        mockApplication = new LoanApplication();
        mockApplication.setId(10L);
        mockApplication.setCustomer(mockCustomer);
        mockApplication.setRequestedAmount(new BigDecimal("200000.00"));
        mockApplication.setTenureMonths(24);
        mockApplication.setStatus(LoanApplicationStatus.SUBMITTED);

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testNoCustomerProfileThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> eligibilityService.evaluate(10L));
    }

    @Test
    void testApplicationNotFoundThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, mockCustomer)).thenReturn(Optional.empty());

        assertThrows(LoanApplicationNotFoundException.class, () -> eligibilityService.evaluate(10L));
    }

    @Test
    void testDraftApplicationThrowsException() {
        mockApplication.setStatus(LoanApplicationStatus.DRAFT);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, mockCustomer)).thenReturn(Optional.of(mockApplication));

        assertThrows(LoanApplicationNotSubmittedException.class, () -> eligibilityService.evaluate(10L));
    }

    @Test
    void testFirstEvaluationCreatesRecord() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, mockCustomer)).thenReturn(Optional.of(mockApplication));

        EligibilityCalculationResult calcResult = new EligibilityCalculationResult(
                EligibilityDecision.ELIGIBLE,
                new BigDecimal("9414.69"),
                new BigDecimal("0.20"),
                new BigDecimal("0.33"),
                List.of("Passed")
        );
        when(eligibilityCalculator.calculate(any(), any(), any(), anyInt())).thenReturn(calcResult);

        when(eligibilityEvaluationRepository.findByLoanApplication(mockApplication)).thenReturn(Optional.empty());

        EligibilityEvaluation savedEvaluation = new EligibilityEvaluation();
        savedEvaluation.setId(100L);
        savedEvaluation.setLoanApplication(mockApplication);
        when(eligibilityEvaluationRepository.save(any(EligibilityEvaluation.class))).thenReturn(savedEvaluation);

        EligibilityEvaluationResponse response = eligibilityService.evaluate(10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        verify(eligibilityEvaluationRepository, times(1)).save(any(EligibilityEvaluation.class));
    }

    @Test
    void testSecondEvaluationUpdatesSameRecord() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, mockCustomer)).thenReturn(Optional.of(mockApplication));

        EligibilityCalculationResult calcResult = new EligibilityCalculationResult(
                EligibilityDecision.ELIGIBLE,
                new BigDecimal("9414.69"),
                new BigDecimal("0.20"),
                new BigDecimal("0.33"),
                List.of("Passed again")
        );
        when(eligibilityCalculator.calculate(any(), any(), any(), anyInt())).thenReturn(calcResult);

        EligibilityEvaluation existingEvaluation = new EligibilityEvaluation();
        existingEvaluation.setId(100L);
        existingEvaluation.setLoanApplication(mockApplication);
        existingEvaluation.setReasons(new ArrayList<>(List.of("Old passed")));

        when(eligibilityEvaluationRepository.findByLoanApplication(mockApplication)).thenReturn(Optional.of(existingEvaluation));
        when(eligibilityEvaluationRepository.save(any(EligibilityEvaluation.class))).thenAnswer(i -> i.getArgument(0));

        EligibilityEvaluationResponse response = eligibilityService.evaluate(10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        
        ArgumentCaptor<EligibilityEvaluation> captor = ArgumentCaptor.forClass(EligibilityEvaluation.class);
        verify(eligibilityEvaluationRepository, times(1)).save(captor.capture());
        
        EligibilityEvaluation saved = captor.getValue();
        assertEquals(100L, saved.getId());
        assertEquals("Passed again", saved.getReasons().get(0));
        assertEquals(1, saved.getReasons().size());
    }

    @Test
    void testGetEvaluation() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, mockCustomer)).thenReturn(Optional.of(mockApplication));

        EligibilityEvaluation existingEvaluation = new EligibilityEvaluation();
        existingEvaluation.setId(100L);
        existingEvaluation.setLoanApplication(mockApplication);
        when(eligibilityEvaluationRepository.findByLoanApplication(mockApplication)).thenReturn(Optional.of(existingEvaluation));

        EligibilityEvaluationResponse response = eligibilityService.getEvaluation(10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void testGetEvaluationNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, mockCustomer)).thenReturn(Optional.of(mockApplication));
        when(eligibilityEvaluationRepository.findByLoanApplication(mockApplication)).thenReturn(Optional.empty());

        assertThrows(EligibilityEvaluationNotFoundException.class, () -> eligibilityService.getEvaluation(10L));
    }
}
