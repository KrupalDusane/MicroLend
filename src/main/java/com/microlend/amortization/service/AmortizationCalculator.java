package com.microlend.amortization.service;

import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.loanapplication.entity.LoanApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AmortizationCalculator {

    private static final BigDecimal ANNUAL_INTEREST_RATE = new BigDecimal("12.00");
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public LoanAmortizationSchedule generateSchedule(LoanApplication loanApplication, LocalDate creationDate) {
        BigDecimal principal = loanApplication.getRequestedAmount();
        Integer tenureMonths = loanApplication.getTenureMonths();

        BigDecimal monthlyRate = ANNUAL_INTEREST_RATE
                .divide(MONTHS_IN_YEAR, MathContext.DECIMAL64)
                .divide(ONE_HUNDRED, MathContext.DECIMAL64);

        BigDecimal emi = calculateEmi(principal, monthlyRate, tenureMonths);

        LoanAmortizationSchedule schedule = new LoanAmortizationSchedule(
                loanApplication,
                ANNUAL_INTEREST_RATE,
                emi,
                tenureMonths,
                BigDecimal.ZERO, // will be updated
                BigDecimal.ZERO, // will be updated
                BigDecimal.ZERO  // will be updated
        );

        List<LoanInstallment> installments = new ArrayList<>();
        BigDecimal currentBalance = principal;
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;

        LocalDate currentDueDate = creationDate.plusMonths(1);

        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interestPortion;
            BigDecimal principalPortion;
            BigDecimal currentEmi;
            BigDecimal closingBalance;

            if (i == tenureMonths) {
                // Final installment adjustment
                interestPortion = currentBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                principalPortion = currentBalance;
                currentEmi = principalPortion.add(interestPortion);
                closingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                interestPortion = currentBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                principalPortion = emi.subtract(interestPortion).setScale(2, RoundingMode.HALF_UP);
                currentEmi = emi;
                closingBalance = currentBalance.subtract(principalPortion).setScale(2, RoundingMode.HALF_UP);
            }

            LoanInstallment installment = new LoanInstallment(
                    i,
                    currentDueDate,
                    currentBalance.setScale(2, RoundingMode.HALF_UP),
                    currentEmi,
                    principalPortion,
                    interestPortion,
                    closingBalance
            );

            installments.add(installment);
            schedule.addInstallment(installment);

            totalPrincipal = totalPrincipal.add(principalPortion);
            totalInterest = totalInterest.add(interestPortion);

            currentBalance = closingBalance;
            currentDueDate = currentDueDate.plusMonths(1);
        }

        schedule.setTotalPrincipal(totalPrincipal);
        schedule.setTotalInterest(totalInterest);
        schedule.setTotalPayable(totalPrincipal.add(totalInterest));

        return schedule;
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal monthlyRate, int n) {
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(n), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow(n, MathContext.DECIMAL64);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
