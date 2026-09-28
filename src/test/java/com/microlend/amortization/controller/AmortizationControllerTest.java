package com.microlend.amortization.controller;

import com.microlend.amortization.dto.AmortizationScheduleResponse;
import com.microlend.amortization.service.AmortizationService;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.common.exception.LoanNotApprovedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AmortizationControllerTest {

    private AmortizationService amortizationService;
    private AmortizationController controller;

    @BeforeEach
    void setUp() {
        amortizationService = Mockito.mock(AmortizationService.class);
        controller = new AmortizationController(amortizationService);
    }

    @Test
    void generateSchedule_success() {
        Principal principal = () -> "test@example.com";
        AmortizationScheduleResponse mockResponse = new AmortizationScheduleResponse();

        when(amortizationService.generateSchedule(1L, "test@example.com")).thenReturn(mockResponse);

        ResponseEntity<AmortizationScheduleResponse> response = controller.generateSchedule(1L, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void generateSchedule_nonApprovedLoan_throwsConflict() {
        Principal principal = () -> "test@example.com";

        when(amortizationService.generateSchedule(1L, "test@example.com"))
                .thenThrow(new LoanNotApprovedException("Not approved"));

        assertThrows(LoanNotApprovedException.class, () -> controller.generateSchedule(1L, principal));
    }

    @Test
    void getSchedule_success() {
        Principal principal = () -> "test@example.com";
        AmortizationScheduleResponse mockResponse = new AmortizationScheduleResponse();

        when(amortizationService.getSchedule(1L, "test@example.com")).thenReturn(mockResponse);

        ResponseEntity<AmortizationScheduleResponse> response = controller.getSchedule(1L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void getSchedule_wrongCustomer_throwsNotFound() {
        Principal principal = () -> "wrong@example.com";

        when(amortizationService.getSchedule(1L, "wrong@example.com"))
                .thenThrow(new LoanApplicationNotFoundException("Not found"));

        assertThrows(LoanApplicationNotFoundException.class, () -> controller.getSchedule(1L, principal));
    }
}
