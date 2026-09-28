package com.microlend.disbursement.repository;

import com.microlend.disbursement.entity.LoanDisbursement;
import com.microlend.loanapplication.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanDisbursementRepository extends JpaRepository<LoanDisbursement, Long> {

    Optional<LoanDisbursement> findByLoanApplication(LoanApplication loanApplication);

    @Modifying
    @Query(value = "UPDATE loan_disbursements SET disbursement_reference = :ref WHERE id = :id", nativeQuery = true)
    void updateDisbursementReferenceNative(@Param("id") Long id, @Param("ref") String ref);

}
