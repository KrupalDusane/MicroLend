package com.microlend.customer.service;

import com.microlend.common.exception.CustomerAlreadyExistsException;
import com.microlend.common.exception.CustomerNotFoundException;
import com.microlend.customer.dto.CustomerCreateRequest;
import com.microlend.customer.dto.CustomerResponse;
import com.microlend.customer.dto.CustomerUpdateRequest;
import com.microlend.customer.entity.Customer;
import com.microlend.customer.repository.CustomerRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerService(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();

        if (customerRepository.existsByUser(currentUser)) {
            throw new CustomerAlreadyExistsException("Customer profile already exists for this user.");
        }

        Customer customer = new Customer(
                currentUser,
                request.getFullName(),
                request.getPhoneNumber(),
                request.getDateOfBirth(),
                request.getAddress(),
                request.getEmploymentType(),
                request.getMonthlyIncome()
        );

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponse(savedCustomer);
    }

    public CustomerResponse getMyProfile() {
        User currentUser = getCurrentAuthenticatedUser();
        Customer customer = customerRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found."));

        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateMyProfile(CustomerUpdateRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        Customer customer = customerRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found."));

        customer.setFullName(request.getFullName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddress(request.getAddress());
        customer.setEmploymentType(request.getEmploymentType());
        customer.setMonthlyIncome(request.getMonthlyIncome());

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponse(updatedCustomer);
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                customer.getDateOfBirth(),
                customer.getAddress(),
                customer.getEmploymentType(),
                customer.getMonthlyIncome(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
