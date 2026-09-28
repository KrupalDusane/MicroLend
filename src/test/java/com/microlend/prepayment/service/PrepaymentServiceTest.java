package com.microlend.prepayment.service;

import com.microlend.amortization.entity.InstallmentStatus;
import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.amortization.repository.LoanAmortizationScheduleRepository;
import com.microlend.customer.entity.Customer;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.disbursement.entity.LoanDisbursement;
import com.microlend.disbursement.repository.LoanDisbursementRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.overdue.repository.LoanPenaltyRepository;
import com.microlend.prepayment.dto.CreatePrepaymentRequest;
import com.microlend.prepayment.entity.LoanPrepayment;
import com.microlend.prepayment.entity.PrepaymentAllocation;
import com.microlend.prepayment.entity.PrepaymentType;
import com.microlend.prepayment.repository.LoanPrepaymentRepository;
import com.microlend.prepayment.repository.PrepaymentAllocationRepository;
import com.microlend.repayment.exception.RepaymentAmountExceedsOutstandingException;
import com.microlend.repayment.repository.RepaymentAllocationRepository;
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
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PrepaymentServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private LoanDisbursementRepository loanDisbursementRepository;
    @Mock
    private LoanAmortizationScheduleRepository scheduleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LoanPrepaymentRepository prepaymentRepository;
    @Mock
    private PrepaymentAllocationRepository prepaymentAllocationRepository;
    @Mock
    private RepaymentAllocationRepository repaymentAllocationRepository;
    @Mock
    private LoanPenaltyRepository penaltyRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PrepaymentService prepaymentService;

    private User officer;
    private LoanApplication loan;
    private LoanDisbursement disbursement;
    private LoanAmortizationSchedule schedule;
    private LoanInstallment installment1;

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
        loan.setStatus(LoanApplicationStatus.APPROVED);
        loan.setApplicationNumber("APP123");

        disbursement = new LoanDisbursement();
        disbursement.setStatus(DisbursementStatus.DISBURSED);

        schedule = new LoanAmortizationSchedule();
        schedule.setTotalPrincipal(new BigDecimal("5000.00"));

        installment1 = new LoanInstallment();
        installment1.setInstallmentNumber(1);
        installment1.setInterestAmount(new BigDecimal("500.00"));
        installment1.setPrincipalAmount(new BigDecimal("5000.00"));
        installment1.setStatus(InstallmentStatus.PENDING);

        schedule.getInstallments().add(installment1);
    }

    @Test
    void processPrepayment_partialOverpayment_throwsException() {
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanDisbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.of(disbursement));
        when(scheduleRepository.findByLoanApplicationForUpdate(loan)).thenReturn(Optional.of(schedule));

        when(repaymentAllocationRepository.sumPrincipalAllocatedByLoanApplicationId(1L)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan)).thenReturn(Optional.empty());
        when(penaltyRepository.findByLoanApplication(loan)).thenReturn(new ArrayList<>());

        // Outstanding principal is 5000, attempting to prepay 6000
        CreatePrepaymentRequest request = new CreatePrepaymentRequest();
        request.setAmount(new BigDecimal("6000.00"));
        request.setPrepaymentType(PrepaymentType.PARTIAL);

        assertThrows(RepaymentAmountExceedsOutstandingException.class, () -> {
            prepaymentService.processPrepayment(1L, request, "officer@example.com");
        });
    }

    @Test
    void processPrepayment_fullUnderpayment_throwsException() {
        when(userRepository.findByEmail("officer@example.com")).thenReturn(Optional.of(officer));
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanDisbursementRepository.findByLoanApplication(loan)).thenReturn(Optional.of(disbursement));
        when(scheduleRepository.findByLoanApplicationForUpdate(loan)).thenReturn(Optional.of(schedule));

        when(repaymentAllocationRepository.sumPrincipalAllocatedByLoanApplicationId(1L)).thenReturn(Optional.empty());
        when(prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan)).thenReturn(Optional.empty());
        when(penaltyRepository.findByLoanApplication(loan)).thenReturn(new ArrayList<>());

        // Settlement amount is 5000 (no overdue interest/penalties), attempting to pay 4000
        CreatePrepaymentRequest request = new CreatePrepaymentRequest();
        request.setAmount(new BigDecimal("4000.00"));
        request.setPrepaymentType(PrepaymentType.FULL);

        assertThrows(RepaymentAmountExceedsOutstandingException.class, () -> {
            prepaymentService.processPrepayment(1L, request, "officer@example.com");
        });
    }
}
