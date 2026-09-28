package com.microlend.eligibility.service;

import com.microlend.eligibility.entity.EligibilityDecision;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class EligibilityCalculator {

    private static final BigDecimal MIN_MONTHLY_INCOME = new BigDecimal("15000.00");
    private static final BigDecimal MAX_LTI_RATIO = new BigDecimal("5.0");
    private static final BigDecimal MAX_DTI_RATIO = new BigDecimal("0.50");
    private static final BigDecimal MAX_EMI_RATIO = new BigDecimal("0.40");
    private static final int MIN_TENURE_MONTHS = 6;
    private static final int MAX_TENURE_MONTHS = 60;
    
    // 12% per annum = 1% per month
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.01");

    public EligibilityCalculationResult calculate(BigDecimal monthlyIncome, BigDecimal existingMonthlyObligations, BigDecimal requestedAmount, int tenureMonths) {
        List<String> reasons = new ArrayList<>();
        boolean eligible = true;

        // Rule 1: Minimum Monthly Income
        if (monthlyIncome.compareTo(MIN_MONTHLY_INCOME) < 0) {
            eligible = false;
            reasons.add("Monthly income is below the minimum eligibility threshold.");
        } else {
            reasons.add("Minimum income requirement passed.");
        }

        // Rule 2: Loan-to-Income Ratio
        BigDecimal annualIncome = monthlyIncome.multiply(new BigDecimal("12"));
        BigDecimal loanToIncomeRatio = requestedAmount.divide(annualIncome, 4, RoundingMode.HALF_UP);
        if (loanToIncomeRatio.compareTo(MAX_LTI_RATIO) > 0) {
            eligible = false;
            reasons.add("Requested loan amount is too high relative to annual income.");
        } else {
            reasons.add("Loan-to-income requirement passed.");
        }

        // Rule 3 & 4: Debt-to-Income Ratio
        BigDecimal debtToIncomeRatio = existingMonthlyObligations.divide(monthlyIncome, 4, RoundingMode.HALF_UP);
        if (debtToIncomeRatio.compareTo(MAX_DTI_RATIO) > 0) {
            eligible = false;
            reasons.add("Existing monthly obligations are too high relative to monthly income.");
        } else {
            reasons.add("Debt-to-income requirement passed.");
        }

        // Rule 5: Tenure Validity
        if (tenureMonths < MIN_TENURE_MONTHS || tenureMonths > MAX_TENURE_MONTHS) {
            eligible = false;
            reasons.add("Loan tenure is outside the allowed eligibility range.");
        } else {
            reasons.add("Tenure requirement passed.");
        }

        // Rule 6: Estimated EMI and EMI-to-Income Ratio
        BigDecimal estimatedEmi = calculateEmi(requestedAmount, tenureMonths);
        BigDecimal emiToIncomeRatio = estimatedEmi.divide(monthlyIncome, 4, RoundingMode.HALF_UP);
        if (emiToIncomeRatio.compareTo(MAX_EMI_RATIO) > 0) {
            eligible = false;
            reasons.add("Estimated EMI is too high relative to monthly income.");
        } else {
            reasons.add("EMI-to-income requirement passed.");
        }

        EligibilityDecision decision = eligible ? EligibilityDecision.ELIGIBLE : EligibilityDecision.NOT_ELIGIBLE;
        
        return new EligibilityCalculationResult(decision, estimatedEmi, debtToIncomeRatio, loanToIncomeRatio, reasons);
    }

    private BigDecimal calculateEmi(BigDecimal principal, int tenureMonths) {
        // EMI = P × r × (1+r)^n / ((1+r)^n - 1)
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal onePlusR = BigDecimal.ONE.add(MONTHLY_INTEREST_RATE);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, mc);
        
        BigDecimal numerator = principal.multiply(MONTHLY_INTEREST_RATE, mc).multiply(onePlusRPowN, mc);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE, mc);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
