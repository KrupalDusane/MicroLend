package com.microlend.prepayment.repository;

import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.prepayment.entity.LoanPrepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanPrepaymentRepository extends JpaRepository<LoanPrepayment, Long> {

    List<LoanPrepayment> findByLoanApplicationOrderByCreatedAtDesc(LoanApplication loanApplication);

    Optional<LoanPrepayment> findByTransactionReference(String transactionReference);

    @Modifying
    @Query(value = "UPDATE loan_prepayments SET transaction_reference = ?2 WHERE id = ?1", nativeQuery = true)
    void updateTransactionReferenceNative(Long id, String reference);
}
