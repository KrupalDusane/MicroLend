package com.microlend.amortization.service;

import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.loanapplication.entity.LoanApplication;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AmortizationCalculatorTest {

    private final AmortizationCalculator calculator = new AmortizationCalculator();

    @Test
    void generateSchedule_basicEmi() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LoanAmortizationSchedule schedule = calculator.generateSchedule(loanApplication, startDate);

        assertNotNull(schedule);
        assertEquals(new BigDecimal("12.00"), schedule.getAnnualInterestRate());
        assertEquals(12, schedule.getNumberOfInstallments());
        // EMI for 100k, 12m, 12% is approx 8884.88
        assertEquals(new BigDecimal("8884.88"), schedule.getMonthlyEmi());
    }

    @Test
    void generateSchedule_24Months() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("200000.00"));
        loanApplication.setTenureMonths(24);

        LoanAmortizationSchedule schedule = calculator.generateSchedule(loanApplication, LocalDate.now());

        assertEquals(24, schedule.getNumberOfInstallments());
        assertEquals(24, schedule.getInstallments().size());
        assertEquals(new BigDecimal("9414.69"), schedule.getMonthlyEmi());
    }

    @Test
    void generateSchedule_finalBalanceIsZero() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        LoanAmortizationSchedule schedule = calculator.generateSchedule(loanApplication, LocalDate.now());

        List<LoanInstallment> installments = schedule.getInstallments();
        LoanInstallment finalInstallment = installments.get(installments.size() - 1);

        assertEquals(new BigDecimal("0.00"), finalInstallment.getClosingBalance());
    }

    @Test
    void generateSchedule_principalConservation() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        LoanAmortizationSchedule schedule = calculator.generateSchedule(loanApplication, LocalDate.now());

        BigDecimal sumPrincipal = schedule.getInstallments().stream()
                .map(LoanInstallment::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(new BigDecimal("100000.00"), sumPrincipal);
        assertEquals(new BigDecimal("100000.00"), schedule.getTotalPrincipal());
    }

    @Test
    void generateSchedule_interestConservation() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        LoanAmortizationSchedule schedule = calculator.generateSchedule(loanApplication, LocalDate.now());

        BigDecimal sumEmi = schedule.getInstallments().stream()
                .map(LoanInstallment::getEmiAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumPrincipalAndInterest = schedule.getTotalPrincipal().add(schedule.getTotalInterest());

        assertEquals(sumPrincipalAndInterest, sumEmi);
        assertEquals(sumPrincipalAndInterest, schedule.getTotalPayable());
    }

    @Test
    void generateSchedule_sequentialBalances() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        LoanAmortizationSchedule schedule = calculator.generateSchedule(loanApplication, LocalDate.now());
        List<LoanInstallment> installments = schedule.getInstallments();

        for (int i = 1; i < installments.size(); i++) {
            LoanInstallment prev = installments.get(i - 1);
            LoanInstallment current = installments.get(i);
            assertEquals(prev.getClosingBalance(), current.getOpeningBalance());
        }
    }

    @Test
    void generateSchedule_noNegativeBalance() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        LoanAmortizationSchedule schedule = calculator.generateSchedule(loanApplication, LocalDate.now());

        for (LoanInstallment installment : schedule.getInstallments()) {
            assertTrue(installment.getClosingBalance().compareTo(BigDecimal.ZERO) >= 0);
        }
    }

    @Test
    void generateSchedule_determinism() {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setRequestedAmount(new BigDecimal("100000.00"));
        loanApplication.setTenureMonths(12);

        LocalDate startDate = LocalDate.of(2026, 9, 1);
        LoanAmortizationSchedule schedule1 = calculator.generateSchedule(loanApplication, startDate);
        LoanAmortizationSchedule schedule2 = calculator.generateSchedule(loanApplication, startDate);

        assertEquals(schedule1.getMonthlyEmi(), schedule2.getMonthlyEmi());
        assertEquals(schedule1.getTotalInterest(), schedule2.getTotalInterest());
    }
}
