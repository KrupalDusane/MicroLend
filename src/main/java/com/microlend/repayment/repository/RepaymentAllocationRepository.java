package com.microlend.repayment.repository;

import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.repayment.entity.RepaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepaymentAllocationRepository extends JpaRepository<RepaymentAllocation, Long> {

    List<RepaymentAllocation> findByInstallment(LoanInstallment installment);

    @Query("SELECT SUM(a.interestAllocated) FROM RepaymentAllocation a WHERE a.installment = :installment")
    Optional<BigDecimal> sumInterestAllocatedByInstallment(@Param("installment") LoanInstallment installment);

    @Query("SELECT SUM(a.principalAllocated) FROM RepaymentAllocation a WHERE a.installment = :installment")
    Optional<BigDecimal> sumPrincipalAllocatedByInstallment(@Param("installment") LoanInstallment installment);

    @Query("SELECT SUM(a.principalAllocated) FROM RepaymentAllocation a WHERE a.repayment.loanApplication.id = :loanApplicationId")
    Optional<BigDecimal> sumPrincipalAllocatedByLoanApplicationId(@Param("loanApplicationId") Long loanApplicationId);

}
