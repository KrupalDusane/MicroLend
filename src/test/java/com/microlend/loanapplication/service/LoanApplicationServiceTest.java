package com.microlend.loanapplication.service;

import com.microlend.common.exception.CustomerProfileRequiredException;
import com.microlend.common.exception.InvalidLoanApplicationStateException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.customer.entity.Customer;
import com.microlend.customer.repository.CustomerRepository;
import com.microlend.loanapplication.dto.LoanApplicationCreateRequest;
import com.microlend.loanapplication.dto.LoanApplicationResponse;
import com.microlend.loanapplication.dto.LoanApplicationUpdateRequest;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.entity.LoanType;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private User testUser;
    private Customer testCustomer;
    private LoanApplication draftApplication;
    private LoanApplication submittedApplication;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@test.com");

        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setUser(testUser);

        draftApplication = new LoanApplication();
        draftApplication.setId(10L);
        draftApplication.setCustomer(testCustomer);
        draftApplication.setStatus(LoanApplicationStatus.DRAFT);
        draftApplication.setApplicationNumber("ML-2026-000010");

        submittedApplication = new LoanApplication();
        submittedApplication.setId(11L);
        submittedApplication.setCustomer(testCustomer);
        submittedApplication.setStatus(LoanApplicationStatus.SUBMITTED);
        submittedApplication.setApplicationNumber("ML-2026-000011");
    }

    @Test
    void testCreateDraft_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(i -> {
            LoanApplication app = i.getArgument(0);
            if (app.getId() == null) {
                app.setId(10L); // first save gets ID
            }
            return app;
        });

        LoanApplicationCreateRequest request = new LoanApplicationCreateRequest(LoanType.PERSONAL, new BigDecimal("1000.00"), 12, "Test Purpose");
        LoanApplicationResponse response = loanApplicationService.createApplication(request, "test@test.com");

        assertNotNull(response);
        assertEquals(LoanApplicationStatus.DRAFT, response.getStatus());
        assertNotNull(response.getApplicationNumber());
    }

    @Test
    void testCreateDraft_NoCustomerProfile_ThrowsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.empty());

        LoanApplicationCreateRequest request = new LoanApplicationCreateRequest(LoanType.PERSONAL, new BigDecimal("1000.00"), 12, "Test Purpose");

        assertThrows(CustomerProfileRequiredException.class, () -> loanApplicationService.createApplication(request, "test@test.com"));
    }

    @Test
    void testUpdateDraft_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, testCustomer)).thenReturn(Optional.of(draftApplication));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(draftApplication);

        LoanApplicationUpdateRequest request = new LoanApplicationUpdateRequest(LoanType.BUSINESS, new BigDecimal("2000.00"), 24, "Updated Purpose");
        LoanApplicationResponse response = loanApplicationService.updateDraftApplication(10L, request, "test@test.com");

        assertNotNull(response);
        assertEquals(LoanType.BUSINESS, draftApplication.getLoanType());
    }

    @Test
    void testUpdateSubmitted_ThrowsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(11L, testCustomer)).thenReturn(Optional.of(submittedApplication));

        LoanApplicationUpdateRequest request = new LoanApplicationUpdateRequest(LoanType.BUSINESS, new BigDecimal("2000.00"), 24, "Updated Purpose");

        assertThrows(InvalidLoanApplicationStateException.class, () -> loanApplicationService.updateDraftApplication(11L, request, "test@test.com"));
    }

    @Test
    void testSubmitDraft_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, testCustomer)).thenReturn(Optional.of(draftApplication));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(draftApplication);

        LoanApplicationResponse response = loanApplicationService.submitApplication(10L, "test@test.com");

        assertNotNull(response);
        assertEquals(LoanApplicationStatus.SUBMITTED, draftApplication.getStatus());
        assertNotNull(draftApplication.getSubmittedAt());
    }

    @Test
    void testSubmitAlreadySubmitted_ThrowsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(11L, testCustomer)).thenReturn(Optional.of(submittedApplication));

        assertThrows(InvalidLoanApplicationStateException.class, () -> loanApplicationService.submitApplication(11L, "test@test.com"));
    }

    @Test
    void testDeleteDraft_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(10L, testCustomer)).thenReturn(Optional.of(draftApplication));

        loanApplicationService.deleteDraftApplication(10L, "test@test.com");
        verify(loanApplicationRepository).delete(draftApplication);
    }

    @Test
    void testDeleteSubmitted_ThrowsException() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(11L, testCustomer)).thenReturn(Optional.of(submittedApplication));

        assertThrows(InvalidLoanApplicationStateException.class, () -> loanApplicationService.deleteDraftApplication(11L, "test@test.com"));
    }

    @Test
    void testGetApplication_WrongCustomer_ThrowsNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        when(loanApplicationRepository.findByIdAndCustomer(999L, testCustomer)).thenReturn(Optional.empty());

        assertThrows(LoanApplicationNotFoundException.class, () -> loanApplicationService.getApplicationById(999L, "test@test.com"));
    }
}
