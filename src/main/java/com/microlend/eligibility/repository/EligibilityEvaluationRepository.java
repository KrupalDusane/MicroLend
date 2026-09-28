package com.microlend.eligibility.repository;

import com.microlend.eligibility.entity.EligibilityEvaluation;
import com.microlend.loanapplication.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EligibilityEvaluationRepository extends JpaRepository<EligibilityEvaluation, Long> {
    Optional<EligibilityEvaluation> findByLoanApplication(LoanApplication loanApplication);
}
