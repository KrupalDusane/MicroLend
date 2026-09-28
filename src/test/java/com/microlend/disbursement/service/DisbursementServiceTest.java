package com.microlend.disbursement.service;

import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.repository.LoanAmortizationScheduleRepository;
import com.microlend.common.exception.LoanAmortizationRequiredException;
import com.microlend.common.exception.LoanNotApprovedForDisbursementException;
import com.microlend.disbursement.dto.LoanDisbursementResponse;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.disbursement.entity.LoanDisbursement;
import com.microlend.disbursement.repository.LoanDisbursementRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.customer.entity.Customer;
import com.microlend.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DisbursementServiceTest {

    private LoanApplicationRepository loanApplicationRepository;
    private LoanAmortizationScheduleRepository scheduleRepository;
    private LoanDisbursementRepository disbursementRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private DisbursementService disbursementService;

    @BeforeEach
    void setUp() {
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        scheduleRepository = mock(LoanAmortizationScheduleRepository.class);
        disbursementRepository = mock(LoanDisbursementRepository.class);
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);

        disbursementService = new DisbursementService(
                loanApplicationRepository,
                scheduleRepository,
                disbursementRepository,
                userRepository,
                notificationService
        );
    }

    @Test
    void disburseLoan_success() {
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setStatus(LoanApplicationStatus.APPROVED);
        loan.setRequestedAmount(new BigDecimal("50000.00"));
        loan.setApplicationNumber("APP-123");
        
        User customerUser = new User();
        customerUser.setId(2L);
        Customer customer = new Customer();
        customer.setUser(customerUser);
        loan.setCustomer(customer);

        User officer = new User();
        officer.setEmail("officer@example.com");

        LoanAmortizationSchedule schedule = new LoanAmortizationSchedule();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(scheduleRepository.findByLoanApplication(loan)).thenReturn(Optional.of(schedule));
        when(disbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));

        LoanDisbursement savedDisbursement = new LoanDisbursement(loan, new BigDecimal("50000.00"), officer, DisbursementStatus.DISBURSED);
        savedDisbursement.setId(10L);

        when(disbursementRepository.saveAndFlush(any(LoanDisbursement.class))).thenReturn(savedDisbursement);

        LoanDisbursementResponse response = disbursementService.disburseLoan(1L, "officer@example.com");

        assertNotNull(response);
        assertEquals(new BigDecimal("50000.00"), response.getDisbursedAmount());
        assertEquals(DisbursementStatus.DISBURSED, response.getStatus());
        verify(disbursementRepository, times(1)).updateDisbursementReferenceNative(anyLong(), anyString());
    }

    @Test
    void disburseLoan_notApproved_throwsException() {
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setStatus(LoanApplicationStatus.SUBMITTED);

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThrows(LoanNotApprovedForDisbursementException.class, () -> disbursementService.disburseLoan(1L, "officer@example.com"));
        verify(disbursementRepository, never()).save(any());
    }

    @Test
    void disburseLoan_noAmortizationSchedule_throwsException() {
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setStatus(LoanApplicationStatus.APPROVED);

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(scheduleRepository.findByLoanApplication(loan)).thenReturn(Optional.empty());

        assertThrows(LoanAmortizationRequiredException.class, () -> disbursementService.disburseLoan(1L, "officer@example.com"));
        verify(disbursementRepository, never()).save(any());
    }

    @Test
    void disburseLoan_alreadyDisbursed_returnsExisting() {
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setStatus(LoanApplicationStatus.APPROVED);
        loan.setRequestedAmount(new BigDecimal("50000.00"));
        loan.setApplicationNumber("APP-123");

        LoanAmortizationSchedule schedule = new LoanAmortizationSchedule();

        LoanDisbursement existingDisbursement = new LoanDisbursement();
        existingDisbursement.setId(1L);
        existingDisbursement.setLoanApplication(loan);
        existingDisbursement.setDisbursedAmount(new BigDecimal("50000.00"));

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(scheduleRepository.findByLoanApplication(loan)).thenReturn(Optional.of(schedule));
        when(disbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.of(existingDisbursement));

        LoanDisbursementResponse response = disbursementService.disburseLoan(1L, "officer@example.com");

        assertNotNull(response);
        verify(disbursementRepository, never()).save(any());
    }

    @Test
    void disburseLoan_concurrentSave_throwsDataIntegrityViolationException() {
        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setStatus(LoanApplicationStatus.APPROVED);
        loan.setRequestedAmount(new BigDecimal("50000.00"));

        User officer = new User();
        officer.setEmail("officer@example.com");

        LoanAmortizationSchedule schedule = new LoanAmortizationSchedule();

        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(scheduleRepository.findByLoanApplication(loan)).thenReturn(Optional.of(schedule));
        when(disbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.empty());

        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));
        when(disbursementRepository.saveAndFlush(any(LoanDisbursement.class))).thenThrow(new DataIntegrityViolationException("Unique constraint"));

        assertThrows(DataIntegrityViolationException.class, () -> disbursementService.disburseLoan(1L, "officer@example.com"));
    }
}
