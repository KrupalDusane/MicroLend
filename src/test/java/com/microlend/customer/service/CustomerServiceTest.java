package com.microlend.customer.service;

import com.microlend.common.exception.CustomerAlreadyExistsException;
import com.microlend.common.exception.CustomerNotFoundException;
import com.microlend.customer.dto.CustomerCreateRequest;
import com.microlend.customer.dto.CustomerResponse;
import com.microlend.customer.dto.CustomerUpdateRequest;
import com.microlend.customer.entity.Customer;
import com.microlend.customer.entity.EmploymentType;
import com.microlend.customer.repository.CustomerRepository;
import com.microlend.user.entity.Role;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerService customerService;

    private User testUser;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com", "hash", Role.CUSTOMER);
        testUser.setId(1L);

        testCustomer = new Customer(testUser, "John Doe", "1234567890", LocalDate.of(1990, 1, 1), "123 Main St", EmploymentType.SALARIED, new BigDecimal("5000.00"));
        testCustomer.setId(10L);

        // Mock Security Context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("john@example.com", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateCustomer_Success() {
        CustomerCreateRequest request = new CustomerCreateRequest("John Doe", "1234567890", LocalDate.of(1990, 1, 1), "123 Main St", EmploymentType.SALARIED, new BigDecimal("5000.00"));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.existsByUser(testUser)).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        CustomerResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals("John Doe", response.getFullName());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testCreateCustomer_DuplicateProfile_ThrowsException() {
        CustomerCreateRequest request = new CustomerCreateRequest("John Doe", "1234567890", LocalDate.of(1990, 1, 1), "123 Main St", EmploymentType.SALARIED, new BigDecimal("5000.00"));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.existsByUser(testUser)).thenReturn(true);

        assertThrows(CustomerAlreadyExistsException.class, () -> customerService.createCustomer(request));
    }

    @Test
    void testGetMyProfile_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));

        CustomerResponse response = customerService.getMyProfile();

        assertNotNull(response);
        assertEquals("John Doe", response.getFullName());
        assertEquals(new BigDecimal("5000.00"), response.getMonthlyIncome());
    }

    @Test
    void testGetMyProfile_NotFound_ThrowsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.getMyProfile());
    }

    @Test
    void testUpdateMyProfile_Success() {
        CustomerUpdateRequest request = new CustomerUpdateRequest("John Updated", "0987654321", LocalDate.of(1990, 1, 1), "456 Updated St", EmploymentType.BUSINESS, new BigDecimal("8000.00"));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(customerRepository.findByUser(testUser)).thenReturn(Optional.of(testCustomer));
        
        Customer updatedCustomer = new Customer(testUser, "John Updated", "0987654321", LocalDate.of(1990, 1, 1), "456 Updated St", EmploymentType.BUSINESS, new BigDecimal("8000.00"));
        updatedCustomer.setId(10L);
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        CustomerResponse response = customerService.updateMyProfile(request);

        assertNotNull(response);
        assertEquals("John Updated", response.getFullName());
        assertEquals("0987654321", response.getPhoneNumber());
        assertEquals(EmploymentType.BUSINESS, response.getEmploymentType());
    }
}
