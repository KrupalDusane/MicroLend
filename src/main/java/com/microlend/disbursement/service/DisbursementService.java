package com.microlend.disbursement.service;

import com.microlend.amortization.repository.LoanAmortizationScheduleRepository;
import com.microlend.common.exception.LoanAmortizationRequiredException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.common.exception.LoanNotApprovedForDisbursementException;
import com.microlend.disbursement.dto.LoanDisbursementResponse;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.disbursement.entity.LoanDisbursement;
import com.microlend.disbursement.repository.LoanDisbursementRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.notification.service.NotificationService;
import com.microlend.notification.entity.NotificationType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class DisbursementService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAmortizationScheduleRepository scheduleRepository;
    private final LoanDisbursementRepository disbursementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DisbursementService(LoanApplicationRepository loanApplicationRepository, LoanAmortizationScheduleRepository scheduleRepository, LoanDisbursementRepository disbursementRepository, UserRepository userRepository, NotificationService notificationService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.scheduleRepository = scheduleRepository;
        this.disbursementRepository = disbursementRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public LoanDisbursementResponse disburseLoan(Long applicationId, String officerEmail) {
        LoanApplication loanApplication = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found."));

        if (loanApplication.getStatus() != LoanApplicationStatus.APPROVED) {
            throw new LoanNotApprovedForDisbursementException("Only APPROVED loans can be disbursed.");
        }

        if (scheduleRepository.findByLoanApplication(loanApplication).isEmpty()) {
            throw new LoanAmortizationRequiredException("An amortization schedule must be generated before disbursement.");
        }

        Optional<LoanDisbursement> existingDisbursement = disbursementRepository.findByLoanApplication(loanApplication);
        if (existingDisbursement.isPresent()) {
            return mapToResponse(existingDisbursement.get());
        }

        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated officer not found."));

        LoanDisbursement disbursement = new LoanDisbursement(
                loanApplication,
                loanApplication.getRequestedAmount(),
                officer,
                DisbursementStatus.DISBURSED
        );

        disbursement = disbursementRepository.saveAndFlush(disbursement);

        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String refNumber = String.format("DISB-%s-%06d", datePart, disbursement.getId());

        disbursementRepository.updateDisbursementReferenceNative(disbursement.getId(), refNumber);
        disbursement.setDisbursementReference(refNumber);

        notificationService.createNotification(
            loanApplication.getCustomer().getUser(),
            NotificationType.LOAN_DISBURSED,
            "Loan Disbursed",
            "Your loan has been successfully disbursed. Amount: ₹" + disbursement.getDisbursedAmount() + ".",
            "DISBURSEMENT",
            disbursement.getId()
        );

        return mapToResponse(disbursement);
    }

    @Transactional(readOnly = true)
    public LoanDisbursementResponse getDisbursement(Long applicationId) {
        LoanApplication loanApplication = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan application not found."));

        LoanDisbursement disbursement = disbursementRepository.findByLoanApplication(loanApplication)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Disbursement record not found."));

        return mapToResponse(disbursement);
    }

    private LoanDisbursementResponse mapToResponse(LoanDisbursement disbursement) {
        return new LoanDisbursementResponse(
                disbursement.getId(),
                disbursement.getDisbursementReference(),
                disbursement.getLoanApplication().getId(),
                disbursement.getLoanApplication().getApplicationNumber(),
                disbursement.getDisbursedAmount(),
                disbursement.getDisbursedAt(),
                disbursement.getStatus()
        );
    }
}
