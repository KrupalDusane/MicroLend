package com.microlend.repayment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microlend.repayment.dto.CreateRepaymentRequest;
import com.microlend.repayment.dto.LoanRepaymentResponse;
import com.microlend.repayment.dto.RepaymentSummaryResponse;
import com.microlend.repayment.service.RepaymentService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class RepaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepaymentService repaymentService;

    @Test
    @WithMockUser(roles = "LOAN_OFFICER", username = "officer@example.com")
    void recordRepayment_asOfficer_returnsCreated() throws Exception {
        CreateRepaymentRequest request = new CreateRepaymentRequest(new BigDecimal("1000.00"));
        when(repaymentService.recordRepayment(anyLong(), any(), anyString())).thenReturn(new LoanRepaymentResponse());

        mockMvc.perform(post("/api/loans/1/repayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void recordRepayment_asCustomer_returnsForbidden() throws Exception {
        CreateRepaymentRequest request = new CreateRepaymentRequest(new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/loans/1/repayments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER", username = "customer@example.com")
    void getRepaymentHistory_asCustomer_returnsOk() throws Exception {
        when(repaymentService.getRepaymentHistory(anyLong(), anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/loans/1/repayments"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER", username = "customer@example.com")
    void getRepaymentSummary_asCustomer_returnsOk() throws Exception {
        when(repaymentService.getRepaymentSummary(anyLong(), anyString())).thenReturn(new RepaymentSummaryResponse());

        mockMvc.perform(get("/api/loans/1/repayment-summary"))
                .andExpect(status().isOk());
    }
}
