package com.microlend.loanapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.microlend.loanapplication.dto.LoanApplicationCreateRequest;
import com.microlend.loanapplication.dto.LoanApplicationResponse;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.entity.LoanType;
import com.microlend.loanapplication.service.LoanApplicationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanApplicationService loanApplicationService;

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void testCreateApplication_Success() throws Exception {
        LoanApplicationCreateRequest request = new LoanApplicationCreateRequest(LoanType.PERSONAL, new BigDecimal("5000.00"), 24, "Car repair");
        
        LoanApplicationResponse mockResponse = new LoanApplicationResponse();
        mockResponse.setId(1L);
        mockResponse.setApplicationNumber("ML-2026-000001");
        mockResponse.setLoanType(LoanType.PERSONAL);
        mockResponse.setRequestedAmount(new BigDecimal("5000.00"));
        mockResponse.setTenureMonths(24);
        mockResponse.setPurpose("Car repair");
        mockResponse.setStatus(LoanApplicationStatus.DRAFT);

        when(loanApplicationService.createApplication(any(LoanApplicationCreateRequest.class), eq("customer@test.com"))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/loan-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationNumber").value("ML-2026-000001"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void testCreateApplication_Unauthenticated_ReturnsUnauthorized() throws Exception {
        LoanApplicationCreateRequest request = new LoanApplicationCreateRequest(LoanType.PERSONAL, new BigDecimal("5000.00"), 24, "Car repair");

        mockMvc.perform(post("/api/loan-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void testCreateApplication_WrongRole_ReturnsForbidden() throws Exception {
        LoanApplicationCreateRequest request = new LoanApplicationCreateRequest(LoanType.PERSONAL, new BigDecimal("5000.00"), 24, "Car repair");

        mockMvc.perform(post("/api/loan-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hacker@test.com", roles = "CUSTOMER")
    void testGetApplication_WrongCustomer_ReturnsNotFound() throws Exception {
        when(loanApplicationService.getApplicationById(eq(999L), eq("hacker@test.com")))
                .thenThrow(new com.microlend.common.exception.LoanApplicationNotFoundException("Loan application not found or does not belong to you"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/loan-applications/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void testCreateApplication_ValidationFailure() throws Exception {
        // Tenure is 0, Amount is 0
        LoanApplicationCreateRequest request = new LoanApplicationCreateRequest(LoanType.PERSONAL, new BigDecimal("0.00"), 0, "");

        mockMvc.perform(post("/api/loan-applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
