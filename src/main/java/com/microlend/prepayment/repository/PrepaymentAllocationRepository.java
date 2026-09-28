package com.microlend.prepayment.repository;

import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.prepayment.entity.PrepaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PrepaymentAllocationRepository extends JpaRepository<PrepaymentAllocation, Long> {

    @Query("SELECT SUM(pa.principalAllocated) FROM PrepaymentAllocation pa WHERE pa.loanPrepayment.loanApplication = :loanApplication")
    Optional<BigDecimal> sumPrincipalAllocatedByLoanApplication(LoanApplication loanApplication);
    
    @Query("SELECT SUM(pa.interestAllocated) FROM PrepaymentAllocation pa WHERE pa.loanPrepayment.loanApplication = :loanApplication")
    Optional<BigDecimal> sumInterestAllocatedByLoanApplication(LoanApplication loanApplication);
    
    @Query("SELECT SUM(pa.penaltyAllocated) FROM PrepaymentAllocation pa WHERE pa.loanPrepayment.loanApplication = :loanApplication")
    Optional<BigDecimal> sumPenaltyAllocatedByLoanApplication(LoanApplication loanApplication);

    @Query("SELECT SUM(pa.interestAllocated) FROM PrepaymentAllocation pa WHERE pa.installment = :installment")
    Optional<BigDecimal> sumInterestAllocatedByInstallment(com.microlend.amortization.entity.LoanInstallment installment);

    @Query("SELECT SUM(pa.penaltyAllocated) FROM PrepaymentAllocation pa WHERE pa.penalty = :penalty")
    Optional<BigDecimal> sumPenaltyAllocatedByPenalty(com.microlend.overdue.entity.LoanPenalty penalty);
}
