package com.microlend.loanofficer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microlend.loanofficer.dto.RejectLoanApplicationRequest;
import com.microlend.loanofficer.service.LoanOfficerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanOfficerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanOfficerService loanOfficerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/loan-officer/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customer_Returns403() throws Exception {
        mockMvc.perform(get("/api/loan-officer/applications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "LOAN_OFFICER")
    void loanOfficer_Allowed() throws Exception {
        mockMvc.perform(get("/api/loan-officer/applications"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "LOAN_OFFICER")
    void reject_FailsValidationIfReasonBlank() throws Exception {
        RejectLoanApplicationRequest request = new RejectLoanApplicationRequest("");

        mockMvc.perform(post("/api/loan-officer/applications/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "LOAN_OFFICER")
    void reject_Succeeds() throws Exception {
        RejectLoanApplicationRequest request = new RejectLoanApplicationRequest("Reasonable cause");

        mockMvc.perform(post("/api/loan-officer/applications/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
