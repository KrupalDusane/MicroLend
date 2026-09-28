package com.microlend.customer.repository;

import com.microlend.customer.entity.Customer;
import com.microlend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser(User user);
    boolean existsByUser(User user);
}
