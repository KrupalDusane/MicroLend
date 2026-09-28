package com.microlend.disbursement.controller;

import com.microlend.common.exception.LoanAmortizationRequiredException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.common.exception.LoanNotApprovedForDisbursementException;
import com.microlend.disbursement.dto.LoanDisbursementResponse;
import com.microlend.disbursement.service.DisbursementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DisbursementControllerTest {

    private DisbursementService disbursementService;
    private DisbursementController controller;

    @BeforeEach
    void setUp() {
        disbursementService = mock(DisbursementService.class);
        controller = new DisbursementController(disbursementService);
    }

    @Test
    void disburseLoan_success() {
        Principal principal = () -> "officer@example.com";
        LoanDisbursementResponse mockResponse = new LoanDisbursementResponse();

        when(disbursementService.disburseLoan(1L, "officer@example.com")).thenReturn(mockResponse);

        ResponseEntity<LoanDisbursementResponse> response = controller.disburseLoan(1L, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void disburseLoan_notApproved_throwsConflict() {
        Principal principal = () -> "officer@example.com";

        when(disbursementService.disburseLoan(1L, "officer@example.com"))
                .thenThrow(new LoanNotApprovedForDisbursementException("Not approved"));

        assertThrows(LoanNotApprovedForDisbursementException.class, () -> controller.disburseLoan(1L, principal));
    }

    @Test
    void disburseLoan_noAmortization_throwsConflict() {
        Principal principal = () -> "officer@example.com";

        when(disbursementService.disburseLoan(1L, "officer@example.com"))
                .thenThrow(new LoanAmortizationRequiredException("Amortization required"));

        assertThrows(LoanAmortizationRequiredException.class, () -> controller.disburseLoan(1L, principal));
    }

    @Test
    void getDisbursement_success() {
        LoanDisbursementResponse mockResponse = new LoanDisbursementResponse();

        when(disbursementService.getDisbursement(1L)).thenReturn(mockResponse);

        ResponseEntity<LoanDisbursementResponse> response = controller.getDisbursement(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void getDisbursement_notFound_throwsException() {
        when(disbursementService.getDisbursement(1L))
                .thenThrow(new LoanApplicationNotFoundException("Not found"));

        assertThrows(LoanApplicationNotFoundException.class, () -> controller.getDisbursement(1L));
    }
}
