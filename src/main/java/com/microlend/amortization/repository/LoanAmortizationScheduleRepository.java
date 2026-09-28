package com.microlend.amortization.repository;

import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.loanapplication.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanAmortizationScheduleRepository extends JpaRepository<LoanAmortizationSchedule, Long> {

    Optional<LoanAmortizationSchedule> findByLoanApplication(LoanApplication loanApplication);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM LoanAmortizationSchedule s WHERE s.loanApplication = :loanApplication")
    Optional<LoanAmortizationSchedule> findByLoanApplicationForUpdate(@org.springframework.data.repository.query.Param("loanApplication") LoanApplication loanApplication);

}
