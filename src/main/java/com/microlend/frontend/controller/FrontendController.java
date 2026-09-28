package com.microlend.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // --- Authentication ---

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    // --- Customer Routes ---

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer/dashboard";
    }

    @GetMapping("/customer/profile")
    public String customerProfile() {
        return "customer/profile";
    }

    @GetMapping("/customer/loans")
    public String customerLoans() {
        return "customer/loans";
    }

    @GetMapping("/customer/loans/new")
    public String customerLoanForm() {
        return "customer/loan-form";
    }

    @GetMapping("/customer/loans/{id}")
    public String customerLoanDetails(@PathVariable Long id) {
        return "customer/loan-details";
    }

    @GetMapping("/customer/notifications")
    public String customerNotifications() {
        return "customer/notifications";
    }

    // --- Officer Routes ---

    @GetMapping("/officer/dashboard")
    public String officerDashboard() {
        return "officer/dashboard";
    }

    @GetMapping("/officer/applications")
    public String officerApplications() {
        return "officer/applications";
    }

    @GetMapping("/officer/applications/{id}")
    public String officerApplicationDetails(@PathVariable Long id) {
        return "officer/application-details";
    }

    @GetMapping("/officer/overdue")
    public String officerOverdue() {
        return "officer/overdue";
    }

    @GetMapping("/officer/notifications")
    public String officerNotifications() {
        return "officer/notifications";
    }
}
