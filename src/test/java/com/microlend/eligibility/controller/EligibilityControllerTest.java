package com.microlend.eligibility.controller;

import com.microlend.eligibility.dto.EligibilityEvaluationResponse;
import com.microlend.eligibility.entity.EligibilityDecision;
import com.microlend.eligibility.service.EligibilityService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EligibilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EligibilityService eligibilityService;

    @Test
    void testUnauthenticatedEvaluateReturns401() throws Exception {
        mockMvc.perform(post("/api/eligibility/applications/1/evaluate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUnauthenticatedGetReturns401() throws Exception {
        mockMvc.perform(get("/api/eligibility/applications/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void testEvaluateSuccess() throws Exception {
        EligibilityEvaluationResponse mockResponse = new EligibilityEvaluationResponse(
                100L, 1L, EligibilityDecision.ELIGIBLE,
                new BigDecimal("50000.00"), new BigDecimal("10000.00"),
                new BigDecimal("200000.00"), 24, new BigDecimal("9414.69"),
                new BigDecimal("0.20"), new BigDecimal("0.33"),
                List.of("Passed"), LocalDateTime.now()
        );

        when(eligibilityService.evaluate(1L)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/eligibility/applications/1/evaluate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("ELIGIBLE"))
                .andExpect(jsonPath("$.loanApplicationId").value(1));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = "CUSTOMER")
    void testGetEvaluationSuccess() throws Exception {
        EligibilityEvaluationResponse mockResponse = new EligibilityEvaluationResponse(
                100L, 1L, EligibilityDecision.ELIGIBLE,
                new BigDecimal("50000.00"), new BigDecimal("10000.00"),
                new BigDecimal("200000.00"), 24, new BigDecimal("9414.69"),
                new BigDecimal("0.20"), new BigDecimal("0.33"),
                List.of("Passed"), LocalDateTime.now()
        );

        when(eligibilityService.getEvaluation(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/eligibility/applications/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.decision").value("ELIGIBLE"))
                .andExpect(jsonPath("$.loanApplicationId").value(1));
    }
}
