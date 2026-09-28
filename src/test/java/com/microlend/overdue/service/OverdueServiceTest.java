package com.microlend.overdue.service;

import com.microlend.amortization.entity.InstallmentStatus;
import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.amortization.repository.LoanInstallmentRepository;
import com.microlend.customer.entity.Customer;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.notification.service.NotificationService;
import com.microlend.overdue.dto.OverdueProcessingResponse;
import com.microlend.overdue.entity.LoanPenalty;
import com.microlend.overdue.repository.LoanPenaltyRepository;
import com.microlend.repayment.repository.RepaymentAllocationRepository;
import com.microlend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OverdueServiceTest {

    @Mock
    private LoanInstallmentRepository installmentRepository;
    @Mock
    private LoanPenaltyRepository penaltyRepository;
    @Mock
    private RepaymentAllocationRepository allocationRepository;
    @Mock
    private NotificationService notificationService;

    private Clock fixedClock;

    private OverdueService overdueService;

    private LoanInstallment overdueInstallment;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-15T10:00:00Z"), ZoneId.of("UTC"));
        overdueService = new OverdueService(installmentRepository, penaltyRepository, allocationRepository, fixedClock, notificationService);

        User customerUser = new User();
        customerUser.setId(2L);
        Customer customer = new Customer();
        customer.setId(100L);
        customer.setUser(customerUser);

        LoanApplication loan = new LoanApplication();
        loan.setId(1L);
        loan.setCustomer(customer);

        LoanAmortizationSchedule schedule = new LoanAmortizationSchedule();
        schedule.setLoanApplication(loan);

        overdueInstallment = new LoanInstallment();
        overdueInstallment.setId(10L);
        overdueInstallment.setInstallmentNumber(1);
        overdueInstallment.setPrincipalAmount(new BigDecimal("1000.00"));
        overdueInstallment.setInterestAmount(new BigDecimal("200.00"));
        overdueInstallment.setDueDate(LocalDate.of(2026, 9, 10)); // 5 days overdue relative to fixedClock
        overdueInstallment.setStatus(InstallmentStatus.PENDING);
        overdueInstallment.setSchedule(schedule);
    }

    @Test
    void processOverdueInstallments_generatesPenalty() {
        when(installmentRepository.findOverdueInstallments(
                eq(DisbursementStatus.DISBURSED),
                any(List.class),
                eq(LocalDate.now(fixedClock))
        )).thenReturn(Collections.singletonList(overdueInstallment));

        when(allocationRepository.sumPrincipalAllocatedByInstallment(overdueInstallment)).thenReturn(Optional.of(BigDecimal.ZERO));
        when(penaltyRepository.existsByInstallmentAndPenaltyDate(overdueInstallment, LocalDate.now(fixedClock))).thenReturn(false);

        LoanPenalty mockPenalty = new LoanPenalty();
        mockPenalty.setId(100L);
        when(penaltyRepository.saveAndFlush(any(LoanPenalty.class))).thenReturn(mockPenalty);

        OverdueProcessingResponse response = overdueService.processOverdueInstallments();

        assertNotNull(response);
        assertEquals(1, response.getProcessedInstallments());
        assertEquals(1, response.getNewPenaltiesCreated());
        // 1000 * 0.001 = 1.00
        assertEquals(new BigDecimal("1.00"), response.getTotalPenaltyCreated());
        assertEquals(InstallmentStatus.OVERDUE, overdueInstallment.getStatus());
        verify(installmentRepository, times(1)).save(overdueInstallment);
        verify(penaltyRepository, times(1)).saveAndFlush(any(LoanPenalty.class));
    }

    @Test
    void processOverdueInstallments_alreadyProcessed_skipsGeneration() {
        when(installmentRepository.findOverdueInstallments(
                eq(DisbursementStatus.DISBURSED),
                any(List.class),
                eq(LocalDate.now(fixedClock))
        )).thenReturn(Collections.singletonList(overdueInstallment));

        when(allocationRepository.sumPrincipalAllocatedByInstallment(overdueInstallment)).thenReturn(Optional.of(BigDecimal.ZERO));
        when(penaltyRepository.existsByInstallmentAndPenaltyDate(overdueInstallment, LocalDate.now(fixedClock))).thenReturn(true);

        OverdueProcessingResponse response = overdueService.processOverdueInstallments();

        assertNotNull(response);
        assertEquals(1, response.getProcessedInstallments());
        assertEquals(0, response.getNewPenaltiesCreated());
        assertEquals(1, response.getAlreadyProcessed());
        verify(penaltyRepository, never()).saveAndFlush(any(LoanPenalty.class));
    }

    @Test
    void processOverdueInstallments_concurrentInsert_handledGracefully() {
        when(installmentRepository.findOverdueInstallments(
                eq(DisbursementStatus.DISBURSED),
                any(List.class),
                eq(LocalDate.now(fixedClock))
        )).thenReturn(Collections.singletonList(overdueInstallment));

        when(allocationRepository.sumPrincipalAllocatedByInstallment(overdueInstallment)).thenReturn(Optional.of(BigDecimal.ZERO));
        when(penaltyRepository.existsByInstallmentAndPenaltyDate(overdueInstallment, LocalDate.now(fixedClock))).thenReturn(false);

        when(penaltyRepository.saveAndFlush(any(LoanPenalty.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        OverdueProcessingResponse response = overdueService.processOverdueInstallments();

        assertNotNull(response);
        assertEquals(1, response.getProcessedInstallments());
        assertEquals(0, response.getNewPenaltiesCreated());
        assertEquals(1, response.getAlreadyProcessed());
    }
}
