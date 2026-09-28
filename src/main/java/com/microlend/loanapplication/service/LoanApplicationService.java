package com.microlend.loanapplication.service;

import com.microlend.common.exception.CustomerProfileRequiredException;
import com.microlend.common.exception.InvalidLoanApplicationStateException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.customer.entity.Customer;
import com.microlend.customer.repository.CustomerRepository;
import com.microlend.loanapplication.dto.LoanApplicationCreateRequest;
import com.microlend.loanapplication.dto.LoanApplicationResponse;
import com.microlend.loanapplication.dto.LoanApplicationUpdateRequest;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.notification.service.NotificationService;
import com.microlend.notification.entity.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public LoanApplicationService(LoanApplicationRepository loanApplicationRepository,
                                  CustomerRepository customerRepository,
                                  UserRepository userRepository,
                                  NotificationService notificationService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private Customer getCustomerFromUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerProfileRequiredException("User not found"));
        return customerRepository.findByUser(user)
                .orElseThrow(() -> new CustomerProfileRequiredException("A Customer profile is required to create or access loan applications."));
    }

    private LoanApplicationResponse mapToResponse(LoanApplication app) {
        return new LoanApplicationResponse(
                app.getId(),
                app.getApplicationNumber(),
                app.getLoanType(),
                app.getRequestedAmount(),
                app.getTenureMonths(),
                app.getPurpose(),
                app.getStatus(),
                app.getCreatedAt(),
                app.getUpdatedAt(),
                app.getSubmittedAt()
        );
    }

    @Transactional
    public LoanApplicationResponse createApplication(LoanApplicationCreateRequest request, String userEmail) {
        Customer customer = getCustomerFromUserEmail(userEmail);

        LoanApplication loanApplication = new LoanApplication(
                customer,
                request.getLoanType(),
                request.getRequestedAmount(),
                request.getTenureMonths(),
                request.getPurpose()
        );
        loanApplication.setApplicationNumber("TEMP-" + java.util.UUID.randomUUID().toString());
        // Save first to generate DB ID
        loanApplication = loanApplicationRepository.save(loanApplication);

        // Generate application number using ID
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String appNumber = String.format("ML-%s-%06d", datePart, loanApplication.getId());
        loanApplication.setApplicationNumber(appNumber);

        // Update DB with native query to bypass updatable=false
        loanApplicationRepository.updateApplicationNumberNative(loanApplication.getId(), appNumber);
        
        // Ensure the Java object reflects the change
        loanApplication.setApplicationNumber(appNumber);

        return mapToResponse(loanApplication);
    }

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getMyApplications(String userEmail) {
        Customer customer = getCustomerFromUserEmail(userEmail);
        return loanApplicationRepository.findByCustomer(customer).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoanApplicationResponse getApplicationById(Long id, String userEmail) {
        Customer customer = getCustomerFromUserEmail(userEmail);
        LoanApplication application = loanApplicationRepository.findByIdAndCustomer(id, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));

        return mapToResponse(application);
    }

    @Transactional
    public LoanApplicationResponse updateDraftApplication(Long id, LoanApplicationUpdateRequest request, String userEmail) {
        Customer customer = getCustomerFromUserEmail(userEmail);
        LoanApplication application = loanApplicationRepository.findByIdAndCustomer(id, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.DRAFT) {
            throw new InvalidLoanApplicationStateException("Only DRAFT applications can be updated.");
        }

        application.setLoanType(request.getLoanType());
        application.setRequestedAmount(request.getRequestedAmount());
        application.setTenureMonths(request.getTenureMonths());
        application.setPurpose(request.getPurpose());

        application = loanApplicationRepository.save(application);
        return mapToResponse(application);
    }

    @Transactional
    public void deleteDraftApplication(Long id, String userEmail) {
        Customer customer = getCustomerFromUserEmail(userEmail);
        LoanApplication application = loanApplicationRepository.findByIdAndCustomer(id, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.DRAFT) {
            throw new InvalidLoanApplicationStateException("Only DRAFT applications can be deleted.");
        }

        loanApplicationRepository.delete(application);
    }

    @Transactional
    public LoanApplicationResponse submitApplication(Long id, String userEmail) {
        Customer customer = getCustomerFromUserEmail(userEmail);
        LoanApplication application = loanApplicationRepository.findByIdAndCustomer(id, customer)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found"));

        if (application.getStatus() != LoanApplicationStatus.DRAFT) {
            throw new InvalidLoanApplicationStateException("Only DRAFT applications can be submitted.");
        }

        application.setStatus(LoanApplicationStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());

        application = loanApplicationRepository.save(application);
        
        notificationService.createNotification(
            customer.getUser(),
            NotificationType.LOAN_APPLICATION_SUBMITTED,
            "Loan Application Submitted",
            "Your loan application " + application.getApplicationNumber() + " has been successfully submitted for review.",
            "LOAN_APPLICATION",
            application.getId()
        );
        
        return mapToResponse(application);
    }
}
