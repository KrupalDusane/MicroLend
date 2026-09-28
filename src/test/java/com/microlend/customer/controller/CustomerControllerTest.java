package com.microlend.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microlend.customer.dto.CustomerCreateRequest;
import com.microlend.customer.dto.CustomerResponse;
import com.microlend.customer.entity.EmploymentType;
import com.microlend.customer.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private CustomerCreateRequest validRequest;
    private CustomerResponse validResponse;

    @BeforeEach
    void setUp() {
        validRequest = new CustomerCreateRequest("John Doe", "1234567890", LocalDate.of(1990, 1, 1), "123 Main St", EmploymentType.SALARIED, new BigDecimal("5000.00"));
        validResponse = new CustomerResponse(1L, "John Doe", "1234567890", LocalDate.of(1990, 1, 1), "123 Main St", EmploymentType.SALARIED, new BigDecimal("5000.00"), null, null);
    }

    @Test
    void testCreateCustomer_WithoutAuth_IsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "CUSTOMER")
    void testCreateCustomer_WithAuth_IsCreated() throws Exception {
        when(customerService.createCustomer(any(CustomerCreateRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "CUSTOMER")
    void testCreateCustomer_ValidationFails() throws Exception {
        // Invalid monthly income (0)
        CustomerCreateRequest invalidRequest = new CustomerCreateRequest("John Doe", "1234567890", LocalDate.of(1990, 1, 1), "123 Main St", EmploymentType.SALARIED, new BigDecimal("0.00"));

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "CUSTOMER")
    void testGetMyProfile_WithAuth_IsOk() throws Exception {
        when(customerService.getMyProfile()).thenReturn(validResponse);

        mockMvc.perform(get("/api/customers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void testGetMyProfile_WithoutAuth_IsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/customers/me"))
                .andExpect(status().isUnauthorized());
    }
}
