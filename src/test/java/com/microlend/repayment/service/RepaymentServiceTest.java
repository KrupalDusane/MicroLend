package com.microlend.repayment.service;

import com.microlend.amortization.entity.InstallmentStatus;
import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.amortization.repository.LoanAmortizationScheduleRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.customer.entity.Customer;
import com.microlend.notification.service.NotificationService;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.disbursement.entity.LoanDisbursement;
import com.microlend.disbursement.repository.LoanDisbursementRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.repayment.dto.CreateRepaymentRequest;
import com.microlend.repayment.dto.LoanRepaymentResponse;
import com.microlend.repayment.entity.LoanRepayment;
import com.microlend.repayment.entity.RepaymentAllocation;
import com.microlend.repayment.entity.RepaymentStatus;
import com.microlend.repayment.exception.LoanNotDisbursedException;
import com.microlend.repayment.exception.RepaymentAmountExceedsOutstandingException;
import com.microlend.repayment.repository.LoanRepaymentRepository;
import com.microlend.repayment.repository.RepaymentAllocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RepaymentServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanDisbursementRepository loanDisbursementRepository;
    @Mock
    private LoanAmortizationScheduleRepository scheduleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LoanRepaymentRepository repaymentRepository;
    @Mock
    private RepaymentAllocationRepository allocationRepository;
    @Mock
    private com.microlend.prepayment.repository.PrepaymentAllocationRepository prepaymentAllocationRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RepaymentService repaymentService;

    private User officer;
    private LoanApplication loan;
    private LoanDisbursement disbursement;
    private LoanAmortizationSchedule schedule;
    private LoanInstallment installment1;
    private LoanInstallment installment2;

    @BeforeEach
    void setUp() {
        officer = new User();
        officer.setEmail("officer@example.com");

        User customerUser = new User();
        customerUser.setId(2L);
        Customer customer = new Customer();
        customer.setId(100L);
        customer.setUser(customerUser);

        loan = new LoanApplication();
        loan.setId(1L);
        loan.setCustomer(customer);

        disbursement = new LoanDisbursement();
        disbursement.setStatus(DisbursementStatus.DISBURSED);

        schedule = new LoanAmortizationSchedule();
        schedule.setTotalPrincipal(new BigDecimal("3100.00"));

        installment1 = new LoanInstallment();
        installment1.setInstallmentNumber(1);
        installment1.setInterestAmount(new BigDecimal("1000.00"));
        installment1.setPrincipalAmount(new BigDecimal("1500.00"));
        installment1.setStatus(InstallmentStatus.PENDING);

        installment2 = new LoanInstallment();
        installment2.setInstallmentNumber(2);
        installment2.setInterestAmount(new BigDecimal("900.00"));
        installment2.setPrincipalAmount(new BigDecimal("1600.00"));
        installment2.setStatus(InstallmentStatus.PENDING);

        schedule.getInstallments().add(installment1);
        schedule.getInstallments().add(installment2);
    }

    @Test
    void recordRepayment_exactInstallment_success() {
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanDisbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.of(disbursement));
        when(scheduleRepository.findByLoanApplicationForUpdate(loan)).thenReturn(Optional.of(schedule));

        when(allocationRepository.sumInterestAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(allocationRepository.sumInterestAllocatedByInstallment(installment2)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByInstallment(installment2)).thenReturn(Optional.empty());

        when(prepaymentAllocationRepository.sumInterestAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumInterestAllocatedByInstallment(installment2)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByLoanApplicationId(1L)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan)).thenReturn(Optional.empty());

        LoanRepayment mockRepayment = new LoanRepayment();
        mockRepayment.setId(1L);
        mockRepayment.setAmount(new BigDecimal("2500.00"));
        mockRepayment.setStatus(RepaymentStatus.SUCCESSFUL);
        mockRepayment.setRecordedBy(officer);
        when(repaymentRepository.save(any(LoanRepayment.class))).thenReturn(mockRepayment);

        RepaymentAllocation mockAllocation = new RepaymentAllocation();
        mockAllocation.setInstallment(installment1);
        when(allocationRepository.save(any(RepaymentAllocation.class))).thenReturn(mockAllocation);

        CreateRepaymentRequest request = new CreateRepaymentRequest(new BigDecimal("2500.00"));

        LoanRepaymentResponse response = repaymentService.recordRepayment(1L, request, "officer@example.com");

        assertNotNull(response);
        assertEquals(InstallmentStatus.PAID, installment1.getStatus());
        assertEquals(InstallmentStatus.PENDING, installment2.getStatus());
        verify(allocationRepository, times(1)).save(any(RepaymentAllocation.class));
    }

    @Test
    void recordRepayment_partialPayment_success() {
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanDisbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.of(disbursement));
        when(scheduleRepository.findByLoanApplicationForUpdate(loan)).thenReturn(Optional.of(schedule));

        when(allocationRepository.sumInterestAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(allocationRepository.sumInterestAllocatedByInstallment(installment2)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByInstallment(installment2)).thenReturn(Optional.empty());

        when(prepaymentAllocationRepository.sumInterestAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumInterestAllocatedByInstallment(installment2)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByLoanApplicationId(1L)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan)).thenReturn(Optional.empty());

        LoanRepayment mockRepayment = new LoanRepayment();
        mockRepayment.setId(1L);
        mockRepayment.setAmount(new BigDecimal("1200.00"));
        mockRepayment.setStatus(RepaymentStatus.SUCCESSFUL);
        mockRepayment.setRecordedBy(officer);
        when(repaymentRepository.save(any(LoanRepayment.class))).thenReturn(mockRepayment);

        RepaymentAllocation mockAllocation = new RepaymentAllocation();
        mockAllocation.setInstallment(installment1);
        when(allocationRepository.save(any(RepaymentAllocation.class))).thenReturn(mockAllocation);

        CreateRepaymentRequest request = new CreateRepaymentRequest(new BigDecimal("1200.00"));

        LoanRepaymentResponse response = repaymentService.recordRepayment(1L, request, "officer@example.com");

        assertNotNull(response);
        assertEquals(InstallmentStatus.PARTIALLY_PAID, installment1.getStatus());
    }

    @Test
    void recordRepayment_overpayment_throwsException() {
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanDisbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.of(disbursement));
        when(scheduleRepository.findByLoanApplicationForUpdate(loan)).thenReturn(Optional.of(schedule));

        when(allocationRepository.sumInterestAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(allocationRepository.sumInterestAllocatedByInstallment(installment2)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByInstallment(installment2)).thenReturn(Optional.empty());

        when(prepaymentAllocationRepository.sumInterestAllocatedByInstallment(installment1)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumInterestAllocatedByInstallment(installment2)).thenReturn(Optional.empty());
        when(allocationRepository.sumPrincipalAllocatedByLoanApplicationId(1L)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan)).thenReturn(Optional.empty());

        // Total outstanding is 2500 + 2500 = 5000
        CreateRepaymentRequest request = new CreateRepaymentRequest(new BigDecimal("5001.00"));

        assertThrows(RepaymentAmountExceedsOutstandingException.class, () -> {
            repaymentService.recordRepayment(1L, request, "officer@example.com");
        });
    }

    @Test
    void recordRepayment_notDisbursed_throwsException() {
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanDisbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.empty());

        CreateRepaymentRequest request = new CreateRepaymentRequest(new BigDecimal("2500.00"));

        assertThrows(LoanNotDisbursedException.class, () -> {
            repaymentService.recordRepayment(1L, request, "officer@example.com");
        });
    }
}
