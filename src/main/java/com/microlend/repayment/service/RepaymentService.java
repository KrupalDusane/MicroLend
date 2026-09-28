package com.microlend.repayment.service;

import com.microlend.amortization.entity.InstallmentStatus;
import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.amortization.repository.LoanAmortizationScheduleRepository;
import com.microlend.common.exception.LoanAmortizationRequiredException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.disbursement.entity.LoanDisbursement;
import com.microlend.disbursement.repository.LoanDisbursementRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.repayment.dto.CreateRepaymentRequest;
import com.microlend.repayment.dto.LoanRepaymentResponse;
import com.microlend.repayment.dto.RepaymentAllocationResponse;
import com.microlend.repayment.dto.RepaymentSummaryResponse;
import com.microlend.repayment.entity.LoanRepayment;
import com.microlend.repayment.entity.RepaymentAllocation;
import com.microlend.repayment.entity.RepaymentStatus;
import com.microlend.repayment.exception.LoanNotDisbursedException;
import com.microlend.repayment.exception.NoOutstandingBalanceException;
import com.microlend.repayment.exception.RepaymentAmountExceedsOutstandingException;
import com.microlend.repayment.repository.LoanRepaymentRepository;
import com.microlend.overdue.entity.LoanPenalty;
import com.microlend.overdue.repository.LoanPenaltyRepository;
import com.microlend.repayment.repository.RepaymentAllocationRepository;
import com.microlend.notification.service.NotificationService;
import com.microlend.notification.entity.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepaymentService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanDisbursementRepository loanDisbursementRepository;
    private final LoanAmortizationScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final RepaymentAllocationRepository allocationRepository;
    private final LoanPenaltyRepository penaltyRepository;
    private final com.microlend.prepayment.repository.PrepaymentAllocationRepository prepaymentAllocationRepository;
    private final NotificationService notificationService;

    public RepaymentService(LoanApplicationRepository loanApplicationRepository,
                            LoanDisbursementRepository loanDisbursementRepository,
                            LoanAmortizationScheduleRepository scheduleRepository,
                            UserRepository userRepository,
                            LoanRepaymentRepository repaymentRepository,
                            RepaymentAllocationRepository allocationRepository,
                            LoanPenaltyRepository penaltyRepository,
                            com.microlend.prepayment.repository.PrepaymentAllocationRepository prepaymentAllocationRepository,
                            NotificationService notificationService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanDisbursementRepository = loanDisbursementRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.repaymentRepository = repaymentRepository;
        this.allocationRepository = allocationRepository;
        this.penaltyRepository = penaltyRepository;
        this.prepaymentAllocationRepository = prepaymentAllocationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public LoanRepaymentResponse recordRepayment(Long applicationId, CreateRepaymentRequest request, String officerEmail) {
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        LoanApplication loan = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan not found"));

        if (loan.getStatus() == com.microlend.loanapplication.entity.LoanApplicationStatus.CLOSED) {
            throw new com.microlend.common.exception.InvalidLoanApplicationStateException("Loan is already closed");
        }

        LoanDisbursement disbursement = loanDisbursementRepository.findByLoanApplication(loan)
                .orElseThrow(() -> new LoanNotDisbursedException("Loan has not been disbursed"));

        if (disbursement.getStatus() != DisbursementStatus.DISBURSED) {
            throw new LoanNotDisbursedException("Loan has not been disbursed");
        }

        LoanAmortizationSchedule schedule = scheduleRepository.findByLoanApplicationForUpdate(loan)
                .orElseThrow(() -> new LoanAmortizationRequiredException("Amortization schedule required for repayment"));

        BigDecimal paymentAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);


        List<LoanInstallment> installments = new ArrayList<>(schedule.getInstallments());
        installments.sort(Comparator.comparing(LoanInstallment::getInstallmentNumber));

        List<InstallmentData> activeInstallments = new ArrayList<>();

        BigDecimal totalInterestOutstanding = BigDecimal.ZERO;
        BigDecimal sumOfInstallmentPrincipalOut = BigDecimal.ZERO;

        for (LoanInstallment inst : installments) {
            BigDecimal interestAllocatedRep = allocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
            BigDecimal interestAllocatedPre = prepaymentAllocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
            BigDecimal interestAllocated = interestAllocatedRep.add(interestAllocatedPre);

            BigDecimal principalAllocated = allocationRepository.sumPrincipalAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);

            BigDecimal interestOut = inst.getInterestAmount().subtract(interestAllocated);
            BigDecimal principalOut = inst.getPrincipalAmount().subtract(principalAllocated);

            totalInterestOutstanding = totalInterestOutstanding.add(interestOut);
            sumOfInstallmentPrincipalOut = sumOfInstallmentPrincipalOut.add(principalOut);
            activeInstallments.add(new InstallmentData(inst, interestOut, principalOut));
        }

        BigDecimal globalPrincipalRepaid = allocationRepository.sumPrincipalAllocatedByLoanApplicationId(loan.getId()).orElse(BigDecimal.ZERO);
        BigDecimal globalPrincipalPrepaid = prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan).orElse(BigDecimal.ZERO);
        BigDecimal globalRemainingPrincipal = schedule.getTotalPrincipal().subtract(globalPrincipalRepaid).subtract(globalPrincipalPrepaid);
        if (globalRemainingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            globalRemainingPrincipal = BigDecimal.ZERO;
        }

        BigDecimal totalOutstanding = totalInterestOutstanding.add(globalRemainingPrincipal);

        if (totalOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            throw new NoOutstandingBalanceException("Loan has no outstanding balance");
        }

        if (paymentAmount.compareTo(totalOutstanding) > 0) {
            throw new RepaymentAmountExceedsOutstandingException("Payment amount exceeds outstanding balance");
        }

        LoanRepayment repayment = new LoanRepayment(loan, paymentAmount, officer, RepaymentStatus.SUCCESSFUL);
        repayment = repaymentRepository.save(repayment);

        BigDecimal remainingPayment = paymentAmount;
        List<RepaymentAllocation> allocations = new ArrayList<>();

        for (InstallmentData data : activeInstallments) {
            if (remainingPayment.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }

            if (data.interestOut.compareTo(BigDecimal.ZERO) > 0 || data.principalOut.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal interestPayment = remainingPayment.min(data.interestOut);
                remainingPayment = remainingPayment.subtract(interestPayment);

                BigDecimal principalPayment = remainingPayment.min(data.principalOut).min(globalRemainingPrincipal);
                remainingPayment = remainingPayment.subtract(principalPayment);
                globalRemainingPrincipal = globalRemainingPrincipal.subtract(principalPayment);

                if (interestPayment.compareTo(BigDecimal.ZERO) > 0 || principalPayment.compareTo(BigDecimal.ZERO) > 0) {
                    RepaymentAllocation allocation = new RepaymentAllocation(repayment, data.installment, interestPayment, principalPayment);
                    allocations.add(allocationRepository.save(allocation));

                    data.interestOut = data.interestOut.subtract(interestPayment);
                    data.principalOut = data.principalOut.subtract(principalPayment);

                    if (data.interestOut.compareTo(BigDecimal.ZERO) == 0 && data.principalOut.compareTo(BigDecimal.ZERO) == 0) {
                        data.installment.setStatus(InstallmentStatus.PAID);
                        
                        notificationService.createNotification(
                            loan.getCustomer().getUser(),
                            NotificationType.INSTALLMENT_PAID,
                            "Installment Paid",
                            "Installment #" + data.installment.getInstallmentNumber() + " has been fully paid.",
                            "INSTALLMENT",
                            data.installment.getId()
                        );
                    } else {
                        if (data.installment.getStatus() != InstallmentStatus.OVERDUE) {
                            data.installment.setStatus(InstallmentStatus.PARTIALLY_PAID);
                        }
                    }
                }
            }
        }

        repaymentRepository.flush();
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String refNumber = String.format("REP-%s-%06d", datePart, repayment.getId());
        repaymentRepository.updateTransactionReferenceNative(repayment.getId(), refNumber);
        repayment.setTransactionReference(refNumber);

        notificationService.createNotification(
            loan.getCustomer().getUser(),
            NotificationType.REPAYMENT_RECEIVED,
            "Repayment Received",
            "We have received your repayment of ₹" + repayment.getAmount() + " with reference " + refNumber + ".",
            "REPAYMENT",
            repayment.getId()
        );

        return mapToResponse(repayment, allocations);
    }

    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> getRepaymentHistory(Long applicationId, String userEmail) {
        LoanApplication loan = getAuthorizedLoanApplication(applicationId, userEmail);

        List<LoanRepayment> repayments = repaymentRepository.findByLoanApplicationOrderByCreatedAtDesc(loan);
        return repayments.stream().map(r -> mapToResponse(r, new ArrayList<>())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepaymentSummaryResponse getRepaymentSummary(Long applicationId, String userEmail) {
        LoanApplication loan = getAuthorizedLoanApplication(applicationId, userEmail);

        LoanAmortizationSchedule schedule = scheduleRepository.findByLoanApplication(loan)
                .orElseThrow(() -> new LoanAmortizationRequiredException("Amortization schedule required"));

        BigDecimal totalPaid = BigDecimal.ZERO;
        List<LoanRepayment> repayments = repaymentRepository.findByLoanApplicationOrderByCreatedAtDesc(loan);
        for(LoanRepayment r : repayments) {
            totalPaid = totalPaid.add(r.getAmount());
        }

        int paidInstallments = 0;
        int partiallyPaidInstallments = 0;
        int remainingInstallments = 0;
        BigDecimal totalOutstanding = BigDecimal.ZERO;

        for (LoanInstallment inst : schedule.getInstallments()) {
            if (inst.getStatus() == InstallmentStatus.PAID) {
                paidInstallments++;
            } else {
                BigDecimal interestAllocatedRep = allocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
                BigDecimal interestAllocatedPre = prepaymentAllocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
                BigDecimal interestAllocated = interestAllocatedRep.add(interestAllocatedPre);

                BigDecimal principalAllocated = allocationRepository.sumPrincipalAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
                
                if (inst.getStatus() == InstallmentStatus.PARTIALLY_PAID || 
                   (inst.getStatus() == InstallmentStatus.OVERDUE && (interestAllocated.compareTo(BigDecimal.ZERO) > 0 || principalAllocated.compareTo(BigDecimal.ZERO) > 0))) {
                    partiallyPaidInstallments++;
                } else {
                    remainingInstallments++;
                }
                
                totalOutstanding = totalOutstanding.add(inst.getInterestAmount().subtract(interestAllocated));
            }
        }
        
        BigDecimal globalPrincipalRepaid = allocationRepository.sumPrincipalAllocatedByLoanApplicationId(loan.getId()).orElse(BigDecimal.ZERO);
        BigDecimal globalPrincipalPrepaid = prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan).orElse(BigDecimal.ZERO);
        BigDecimal globalRemainingPrincipal = schedule.getTotalPrincipal().subtract(globalPrincipalRepaid).subtract(globalPrincipalPrepaid);
        if (globalRemainingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            globalRemainingPrincipal = BigDecimal.ZERO;
        }
        totalOutstanding = totalOutstanding.add(globalRemainingPrincipal);

        BigDecimal totalPenalties = BigDecimal.ZERO;
        List<LoanPenalty> penalties = penaltyRepository.findByLoanApplication(loan);
        for(LoanPenalty p : penalties) {
            BigDecimal penPrepaid = prepaymentAllocationRepository.sumPenaltyAllocatedByPenalty(p).orElse(BigDecimal.ZERO);
            BigDecimal remainingPen = p.getPenaltyAmount().subtract(penPrepaid);
            if (remainingPen.compareTo(BigDecimal.ZERO) > 0) {
                totalPenalties = totalPenalties.add(remainingPen);
                totalOutstanding = totalOutstanding.add(remainingPen);
            }
        }

        return new RepaymentSummaryResponse(
                schedule.getTotalPayable(),
                totalPaid,
                totalOutstanding,
                paidInstallments,
                partiallyPaidInstallments,
                remainingInstallments,
                totalPenalties
        );
    }

    private LoanApplication getAuthorizedLoanApplication(Long applicationId, String userEmail) {
        LoanApplication loan = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == com.microlend.user.entity.Role.CUSTOMER) {
            if (!loan.getCustomer().getUser().getId().equals(user.getId())) {
                throw new LoanApplicationNotFoundException("Loan not found");
            }
        }
        return loan;
    }

    private LoanRepaymentResponse mapToResponse(LoanRepayment repayment, List<RepaymentAllocation> allocations) {
        List<RepaymentAllocationResponse> allocationResponses = allocations.stream().map(a ->
                new RepaymentAllocationResponse(
                        a.getInstallment().getInstallmentNumber(),
                        a.getInterestAllocated(),
                        a.getPrincipalAllocated(),
                        a.getAllocatedAmount()
                )).collect(Collectors.toList());

        return new LoanRepaymentResponse(
                repayment.getTransactionReference(),
                repayment.getAmount(),
                repayment.getPaidAt(),
                repayment.getStatus(),
                repayment.getRecordedBy().getEmail(), // Or name
                allocationResponses
        );
    }

    private static class InstallmentData {
        LoanInstallment installment;
        BigDecimal interestOut;
        BigDecimal principalOut;

        InstallmentData(LoanInstallment installment, BigDecimal interestOut, BigDecimal principalOut) {
            this.installment = installment;
            this.interestOut = interestOut;
            this.principalOut = principalOut;
        }
    }
}
