package com.microlend.overdue.repository;

import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.overdue.entity.LoanPenalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanPenaltyRepository extends JpaRepository<LoanPenalty, Long> {

    boolean existsByInstallmentAndPenaltyDate(LoanInstallment installment, LocalDate penaltyDate);

    List<LoanPenalty> findByLoanApplication(LoanApplication loanApplication);
    
    List<LoanPenalty> findByInstallment(LoanInstallment installment);
}
