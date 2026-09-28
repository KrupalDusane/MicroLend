package com.microlend.loanapplication.repository;

import com.microlend.customer.entity.Customer;
import com.microlend.loanapplication.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByCustomer(Customer customer);

    Optional<LoanApplication> findByIdAndCustomer(Long id, Customer customer);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE loan_applications SET application_number = :appNumber WHERE id = :id", nativeQuery = true)
    void updateApplicationNumberNative(@org.springframework.data.repository.query.Param("id") Long id, @org.springframework.data.repository.query.Param("appNumber") String appNumber);

    List<LoanApplication> findByStatusInOrderBySubmittedAtAsc(List<com.microlend.loanapplication.entity.LoanApplicationStatus> statuses);
}
