package com.microlend.amortization.service;

import com.microlend.amortization.dto.AmortizationScheduleResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AmortizationServiceTest {

    private LoanAmortizationScheduleRepository scheduleRepository;
    private AmortizationCalculator calculator;
    private LoanApplicationRepository loanApplicationRepository;
    private CustomerRepository customerRepository;
    private UserRepository userRepository;
    private AmortizationService amortizationService;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(LoanAmortizationScheduleRepository.class);
        calculator = mock(AmortizationCalculator.class);
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        customerRepository = mock(CustomerRepository.class);
        userRepository = mock(UserRepository.class);

        amortizationService = new AmortizationService(
                scheduleRepository,
                calculator,
                loanApplicationRepository,
                customerRepository,
                userRepository
        );
    }

    @Test
    void generateSchedule_approvedLoan_generatesSchedule() {
        User user = new User();
        user.setEmail("test@example.com");

        Customer customer = new Customer();
        customer.setUser(user);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(1L);
        loanApplication.setStatus(LoanApplicationStatus.APPROVED);
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerRepository.findByUser(any(User.class))).thenReturn(Optional.of(customer));
        when(loanApplicationRepository.findByIdAndCustomer(1L, customer)).thenReturn(Optional.of(loanApplication));
        when(scheduleRepository.findByLoanApplication(loanApplication)).thenReturn(Optional.empty());

        LoanAmortizationSchedule mockedSchedule = new LoanAmortizationSchedule(
                loanApplication,
                new BigDecimal("12.00"),
                new BigDecimal("8884.88"),
                12,
                new BigDecimal("100000.00"),
                new BigDecimal("6618.56"),
                new BigDecimal("106618.56")
        );
        when(calculator.generateSchedule(any(LoanApplication.class), any(LocalDate.class))).thenReturn(mockedSchedule);
        when(scheduleRepository.save(any(LoanAmortizationSchedule.class))).thenReturn(mockedSchedule);

        AmortizationScheduleResponse response = amortizationService.generateSchedule(1L, "test@example.com");

        assertNotNull(response);
        assertEquals(new BigDecimal("100000.00"), response.getPrincipal());
        verify(calculator, times(1)).generateSchedule(any(LoanApplication.class), any(LocalDate.class));
        verify(scheduleRepository, times(1)).save(any(LoanAmortizationSchedule.class));
    }

    @Test
    void generateSchedule_notApproved_throwsException() {
        User user = new User();
        user.setEmail("test@example.com");

        Customer customer = new Customer();
        customer.setUser(user);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(1L);
        loanApplication.setStatus(LoanApplicationStatus.UNDER_REVIEW);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerRepository.findByUser(any(User.class))).thenReturn(Optional.of(customer));
        when(loanApplicationRepository.findByIdAndCustomer(1L, customer)).thenReturn(Optional.of(loanApplication));

        assertThrows(LoanNotApprovedException.class, () -> amortizationService.generateSchedule(1L, "test@example.com"));

        verify(calculator, never()).generateSchedule(any(), any());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void generateSchedule_existingSchedule_returnsExisting() {
        User user = new User();
        user.setEmail("test@example.com");

        Customer customer = new Customer();
        customer.setUser(user);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(1L);
        loanApplication.setStatus(LoanApplicationStatus.APPROVED);

        LoanAmortizationSchedule existingSchedule = new LoanAmortizationSchedule(
                loanApplication,
                new BigDecimal("12.00"),
                new BigDecimal("8884.88"),
                12,
                new BigDecimal("100000.00"),
                new BigDecimal("6618.56"),
                new BigDecimal("106618.56")
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerRepository.findByUser(any(User.class))).thenReturn(Optional.of(customer));
        when(loanApplicationRepository.findByIdAndCustomer(1L, customer)).thenReturn(Optional.of(loanApplication));
        when(scheduleRepository.findByLoanApplication(loanApplication)).thenReturn(Optional.of(existingSchedule));

        AmortizationScheduleResponse response = amortizationService.generateSchedule(1L, "test@example.com");

        assertNotNull(response);
        verify(calculator, never()).generateSchedule(any(), any());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void getSchedule_wrongCustomer_throwsNotFound() {
        User user = new User();
        user.setEmail("wrong@example.com");

        Customer customer = new Customer();
        customer.setUser(user);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(customerRepository.findByUser(any(User.class))).thenReturn(Optional.of(customer));
        when(loanApplicationRepository.findByIdAndCustomer(1L, customer)).thenReturn(Optional.empty());

        assertThrows(LoanApplicationNotFoundException.class, () -> amortizationService.getSchedule(1L, "wrong@example.com"));
    }
}
