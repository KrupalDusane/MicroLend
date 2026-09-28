package com.microlend.customer.controller;

import com.microlend.customer.dto.CustomerCreateRequest;
import com.microlend.customer.dto.CustomerResponse;
import com.microlend.customer.dto.CustomerUpdateRequest;
import com.microlend.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Customers", description = "Endpoints for managing customer profiles")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create customer profile", description = "Creates a customer profile for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer profile created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "409", description = "Customer profile already exists")
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get customer profile", description = "Retrieves the customer profile for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer profile retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Customer profile not found")
    })
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile() {
        CustomerResponse response = customerService.getMyProfile();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update customer profile", description = "Updates the customer profile for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Customer profile not found")
    })
    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(@Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse response = customerService.updateMyProfile(request);
        return ResponseEntity.ok(response);
    }
}
