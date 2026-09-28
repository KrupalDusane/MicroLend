package com.microlend.customer.dto;

import com.microlend.customer.entity.EmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer profile information")
public class CustomerResponse {

    @Schema(description = "Unique customer ID", example = "100")
    private Long id;
    
    @Schema(description = "Full legal name of the customer", example = "Jane Doe")
    private String fullName;
    
    @Schema(description = "Contact phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Date of birth", example = "1990-05-15")
    private LocalDate dateOfBirth;
    
    @Schema(description = "Residential address", example = "123 Main St, Springfield")
    private String address;
    
    @Schema(description = "Current employment status", example = "SALARIED")
    private EmploymentType employmentType;
    
    @Schema(description = "Verified monthly income", example = "5000.00")
    private BigDecimal monthlyIncome;
    
    @Schema(description = "Timestamp when the profile was created", example = "2026-09-13T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the profile was last updated", example = "2026-09-13T10:00:00")
    private LocalDateTime updatedAt;

    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String fullName, String phoneNumber, LocalDate dateOfBirth, String address, EmploymentType employmentType, BigDecimal monthlyIncome, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.employmentType = employmentType;
        this.monthlyIncome = monthlyIncome;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
