package com.microlend.eligibility.service;

import com.microlend.eligibility.entity.EligibilityDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

class EligibilityCalculatorTest {

    private EligibilityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EligibilityCalculator();
    }

    @Test
    void testPerfectEligibility() {
        // income = 50000, obligations = 10000, requested = 200000, tenure = 24
        // LTI = 200000 / 600000 = 0.3333
        // DTI = 10000 / 50000 = 0.20
        // EMI = 200000 * 0.01 * 1.01^24 / (1.01^24 - 1) ~ 9414.69
        // EMI Ratio = 9414.69 / 50000 ~ 0.1883
        
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("200000.00"),
                24
        );
        
        assertEquals(EligibilityDecision.ELIGIBLE, result.getDecision());
        assertEquals(0, result.getReasons().stream().filter(r -> r.contains("too high") || r.contains("below")).count());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Minimum income requirement passed.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Loan-to-income requirement passed.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Debt-to-income requirement passed.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Tenure requirement passed.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("EMI-to-income requirement passed.")));
        
        assertEquals(new BigDecimal("9414.69"), result.getEstimatedEmi());
        assertEquals(new BigDecimal("0.2000"), result.getDebtToIncomeRatio());
        assertEquals(new BigDecimal("0.3333"), result.getLoanToIncomeRatio());
    }

    @Test
    void testIncomeExactly15000() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("15000.00"),
                BigDecimal.ZERO,
                new BigDecimal("50000.00"),
                24
        );
        assertEquals(EligibilityDecision.ELIGIBLE, result.getDecision());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Minimum income requirement passed.")));
    }

    @Test
    void testIncomeBelow15000() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("14999.99"),
                BigDecimal.ZERO,
                new BigDecimal("50000.00"),
                24
        );
        assertEquals(EligibilityDecision.NOT_ELIGIBLE, result.getDecision());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Monthly income is below the minimum eligibility threshold.")));
    }

    @Test
    void testLtiExactly5() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("20000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1200000.00"),
                60
        );
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Loan-to-income requirement passed.")));
    }

    @Test
    void testLtiAbove5() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("20000.00"),
                BigDecimal.ZERO,
                new BigDecimal("1200015.00"), // Updated to make ratio > 5.0000 after rounding
                60
        );
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Requested loan amount is too high relative to annual income.")));
    }

    @Test
    void testDtiExactlyHalf() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("30000.00"),
                new BigDecimal("15000.00"),
                new BigDecimal("50000.00"),
                24
        );
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Debt-to-income requirement passed.")));
    }

    @Test
    void testDtiAboveHalf() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("30000.00"),
                new BigDecimal("15015.00"), // Updated to make ratio > 0.5000 after rounding
                new BigDecimal("50000.00"),
                24
        );
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Existing monthly obligations are too high")));
    }

    @Test
    void testTenureExactly6() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                new BigDecimal("10000.00"),
                6
        );
        assertEquals(EligibilityDecision.ELIGIBLE, result.getDecision());
    }

    @Test
    void testTenureExactly60() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                new BigDecimal("10000.00"),
                60
        );
        assertEquals(EligibilityDecision.ELIGIBLE, result.getDecision());
    }

    @Test
    void testTenureBelow6() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                new BigDecimal("10000.00"),
                5
        );
        assertEquals(EligibilityDecision.NOT_ELIGIBLE, result.getDecision());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Loan tenure is outside the allowed eligibility range.")));
    }

    @Test
    void testTenureAbove60() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("50000.00"),
                BigDecimal.ZERO,
                new BigDecimal("10000.00"),
                61
        );
        assertEquals(EligibilityDecision.NOT_ELIGIBLE, result.getDecision());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Loan tenure is outside the allowed eligibility range.")));
    }

    @Test
    void testEmiRatioAbove40() {
        // income = 20000, req = 500000, tenure = 12
        // EMI is roughly 500000 / 12 = 41666 > 20000
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("20000.00"),
                BigDecimal.ZERO,
                new BigDecimal("500000.00"),
                12
        );
        assertEquals(EligibilityDecision.NOT_ELIGIBLE, result.getDecision());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Estimated EMI is too high relative to monthly income.")));
    }

    @Test
    void testMultipleSimultaneousFailures() {
        EligibilityCalculationResult result = calculator.calculate(
                new BigDecimal("10000.00"), // fails Income
                new BigDecimal("6000.00"),  // fails DTI
                new BigDecimal("1000000.00"), // fails LTI
                5 // fails Tenure
        );
        assertEquals(EligibilityDecision.NOT_ELIGIBLE, result.getDecision());
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Monthly income is below the minimum eligibility threshold.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Requested loan amount is too high relative to annual income.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Existing monthly obligations are too high relative to monthly income.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Loan tenure is outside the allowed eligibility range.")));
        assertTrue(result.getReasons().stream().anyMatch(r -> r.contains("Estimated EMI is too high relative to monthly income.")));
    }

    @Test
    void testDeterministicRepeatedCalculations() {
        EligibilityCalculationResult result1 = calculator.calculate(
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("200000.00"),
                24
        );
        EligibilityCalculationResult result2 = calculator.calculate(
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                new BigDecimal("200000.00"),
                24
        );
        assertEquals(result1.getDecision(), result2.getDecision());
        assertEquals(result1.getEstimatedEmi(), result2.getEstimatedEmi());
        assertEquals(result1.getDebtToIncomeRatio(), result2.getDebtToIncomeRatio());
        assertEquals(result1.getLoanToIncomeRatio(), result2.getLoanToIncomeRatio());
        assertEquals(result1.getReasons(), result2.getReasons());
    }
}
