package com.microlend.amortization.repository;

import com.microlend.amortization.entity.InstallmentStatus;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.disbursement.entity.DisbursementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {

    @Query("SELECT i FROM LoanInstallment i " +
           "JOIN i.schedule s " +
           "JOIN LoanDisbursement d ON d.loanApplication = s.loanApplication " +
           "WHERE d.status = :disbursedStatus " +
           "AND i.status IN (:statuses) " +
           "AND i.dueDate < :businessDate")
    List<LoanInstallment> findOverdueInstallments(
            @Param("disbursedStatus") DisbursementStatus disbursedStatus,
            @Param("statuses") List<InstallmentStatus> statuses,
            @Param("businessDate") LocalDate businessDate);
}
