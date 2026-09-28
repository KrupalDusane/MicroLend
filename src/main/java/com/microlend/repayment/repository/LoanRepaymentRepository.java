package com.microlend.repayment.repository;

import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.repayment.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanApplicationOrderByCreatedAtDesc(LoanApplication loanApplication);

    @Modifying
    @Query(value = "UPDATE loan_repayments SET transaction_reference = :reference WHERE id = :id", nativeQuery = true)
    void updateTransactionReferenceNative(@Param("id") Long id, @Param("reference") String reference);
}
