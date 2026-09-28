package com.microlend.prepayment.service;

import com.microlend.amortization.entity.InstallmentStatus;
import com.microlend.amortization.entity.LoanAmortizationSchedule;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.amortization.repository.LoanAmortizationScheduleRepository;
import com.microlend.common.exception.InvalidLoanApplicationStateException;
import com.microlend.common.exception.LoanAmortizationRequiredException;
import com.microlend.common.exception.LoanApplicationNotFoundException;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.disbursement.entity.LoanDisbursement;
import com.microlend.disbursement.repository.LoanDisbursementRepository;
import com.microlend.loanapplication.entity.LoanApplication;
import com.microlend.loanapplication.entity.LoanApplicationStatus;
import com.microlend.loanapplication.repository.LoanApplicationRepository;
import com.microlend.overdue.entity.LoanPenalty;
import com.microlend.overdue.entity.PenaltyStatus;
import com.microlend.overdue.repository.LoanPenaltyRepository;
import com.microlend.prepayment.dto.CreatePrepaymentRequest;
import com.microlend.prepayment.dto.PrepaymentAllocationResponse;
import com.microlend.prepayment.dto.PrepaymentResponse;
import com.microlend.prepayment.dto.SettlementQuoteResponse;
import com.microlend.prepayment.entity.LoanPrepayment;
import com.microlend.prepayment.entity.PrepaymentAllocation;
import com.microlend.prepayment.entity.PrepaymentType;
import com.microlend.prepayment.repository.LoanPrepaymentRepository;
import com.microlend.prepayment.repository.PrepaymentAllocationRepository;
import com.microlend.repayment.exception.LoanNotDisbursedException;
import com.microlend.repayment.exception.RepaymentAmountExceedsOutstandingException;
import com.microlend.repayment.repository.RepaymentAllocationRepository;
import com.microlend.user.entity.Role;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import com.microlend.notification.service.NotificationService;
import com.microlend.notification.entity.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrepaymentService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanDisbursementRepository loanDisbursementRepository;
    private final LoanAmortizationScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final LoanPrepaymentRepository prepaymentRepository;
    private final PrepaymentAllocationRepository prepaymentAllocationRepository;
    private final RepaymentAllocationRepository repaymentAllocationRepository;
    private final LoanPenaltyRepository penaltyRepository;
    private final NotificationService notificationService;

    public PrepaymentService(LoanApplicationRepository loanApplicationRepository,
                             LoanDisbursementRepository loanDisbursementRepository,
                             LoanAmortizationScheduleRepository scheduleRepository,
                             UserRepository userRepository,
                             LoanPrepaymentRepository prepaymentRepository,
                             PrepaymentAllocationRepository prepaymentAllocationRepository,
                             RepaymentAllocationRepository repaymentAllocationRepository,
                             LoanPenaltyRepository penaltyRepository,
                             NotificationService notificationService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanDisbursementRepository = loanDisbursementRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.prepaymentRepository = prepaymentRepository;
        this.prepaymentAllocationRepository = prepaymentAllocationRepository;
        this.repaymentAllocationRepository = repaymentAllocationRepository;
        this.penaltyRepository = penaltyRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public SettlementQuoteResponse getSettlementQuote(Long applicationId, String userEmail) {
        LoanApplication loan = getAuthorizedLoanApplication(applicationId, userEmail);
        
        if (loan.getStatus() == LoanApplicationStatus.CLOSED) {
            return new SettlementQuoteResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDateTime.now());
        }

        LoanDisbursement disbursement = loanDisbursementRepository.findByLoanApplication(loan)
                .orElseThrow(() -> new LoanNotDisbursedException("Loan not disbursed"));
        if (disbursement.getStatus() != DisbursementStatus.DISBURSED) {
            throw new LoanNotDisbursedException("Loan not disbursed");
        }

        LoanAmortizationSchedule schedule = scheduleRepository.findByLoanApplication(loan)
                .orElseThrow(() -> new LoanAmortizationRequiredException("Amortization schedule required"));

        return calculateSettlement(loan, schedule);
    }

    @Transactional
    public PrepaymentResponse processPrepayment(Long applicationId, CreatePrepaymentRequest request, String officerEmail) {
        User officer = userRepository.findByEmail(officerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        LoanApplication loan = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan not found"));

        if (loan.getStatus() == LoanApplicationStatus.CLOSED) {
            throw new InvalidLoanApplicationStateException("Loan is already closed");
        }

        LoanDisbursement disbursement = loanDisbursementRepository.findByLoanApplication(loan)
                .orElseThrow(() -> new LoanNotDisbursedException("Loan not disbursed"));
        if (disbursement.getStatus() != DisbursementStatus.DISBURSED) {
            throw new LoanNotDisbursedException("Loan not disbursed");
        }

        LoanAmortizationSchedule schedule = scheduleRepository.findByLoanApplicationForUpdate(loan)
                .orElseThrow(() -> new LoanAmortizationRequiredException("Amortization schedule required"));

        BigDecimal requestedAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
        
        SettlementQuoteResponse quote = calculateSettlement(loan, schedule);

        if (request.getPrepaymentType() == PrepaymentType.PARTIAL) {
            if (requestedAmount.compareTo(quote.getOutstandingPrincipal()) > 0) {
                throw new RepaymentAmountExceedsOutstandingException("Partial prepayment exceeds outstanding principal");
            }
            
            LoanPrepayment prepayment = new LoanPrepayment(loan, requestedAmount, PrepaymentType.PARTIAL, officer);
            prepayment = prepaymentRepository.save(prepayment);
            
            PrepaymentAllocation allocation = new PrepaymentAllocation(prepayment, null, null, requestedAmount, BigDecimal.ZERO, BigDecimal.ZERO);
            allocation = prepaymentAllocationRepository.save(allocation);
            
            prepaymentRepository.flush();
            String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String refNumber = String.format("PRE-%s-%06d", datePart, prepayment.getId());
            prepaymentRepository.updateTransactionReferenceNative(prepayment.getId(), refNumber);
            prepayment.setTransactionReference(refNumber);
            
            notificationService.createNotification(
                loan.getCustomer().getUser(),
                NotificationType.PARTIAL_PREPAYMENT_RECEIVED,
                "Partial Prepayment Received",
                "We have received your partial prepayment of ₹" + requestedAmount + " with reference " + refNumber + ".",
                "PREPAYMENT",
                prepayment.getId()
            );
            
            if (requestedAmount.compareTo(quote.getOutstandingPrincipal()) == 0 && quote.getOutstandingInterest().compareTo(BigDecimal.ZERO) == 0 && quote.getActivePenalties().compareTo(BigDecimal.ZERO) == 0) {
                 loan.setStatus(LoanApplicationStatus.CLOSED);
                 
                 notificationService.createNotification(
                     loan.getCustomer().getUser(),
                     NotificationType.LOAN_CLOSED,
                     "Loan Closed",
                     "Your loan application " + loan.getApplicationNumber() + " has been successfully closed.",
                     "PREPAYMENT",
                     prepayment.getId()
                 );
            }

            return mapToResponse(prepayment, List.of(allocation));

        } else {
            // FULL
            if (requestedAmount.compareTo(quote.getSettlementAmount()) != 0) {
                throw new RepaymentAmountExceedsOutstandingException("Full prepayment amount must exactly match the settlement amount");
            }
            
            LoanPrepayment prepayment = new LoanPrepayment(loan, requestedAmount, PrepaymentType.FULL, officer);
            prepayment = prepaymentRepository.save(prepayment);
            
            List<PrepaymentAllocation> allocations = new ArrayList<>();
            
            // 1. Penalties
            BigDecimal remainingPenaltyToAllocate = quote.getActivePenalties();
            if (remainingPenaltyToAllocate.compareTo(BigDecimal.ZERO) > 0) {
                List<LoanPenalty> activePenalties = penaltyRepository.findByLoanApplication(loan)
                        .stream().filter(p -> p.getStatus() == PenaltyStatus.ACTIVE)
                        .sorted(Comparator.comparing(LoanPenalty::getPenaltyDate))
                        .collect(Collectors.toList());
                        
                for (LoanPenalty penalty : activePenalties) {
                    if (remainingPenaltyToAllocate.compareTo(BigDecimal.ZERO) <= 0) break;
                    
                    BigDecimal penaltyAllocatedAlready = prepaymentAllocationRepository.sumPenaltyAllocatedByPenalty(penalty).orElse(BigDecimal.ZERO);
                    BigDecimal penaltyOutstanding = penalty.getPenaltyAmount().subtract(penaltyAllocatedAlready);
                    
                    if (penaltyOutstanding.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal toAllocate = remainingPenaltyToAllocate.min(penaltyOutstanding);
                        PrepaymentAllocation alloc = new PrepaymentAllocation(prepayment, null, penalty, BigDecimal.ZERO, BigDecimal.ZERO, toAllocate);
                        allocations.add(prepaymentAllocationRepository.save(alloc));
                        remainingPenaltyToAllocate = remainingPenaltyToAllocate.subtract(toAllocate);
                    }
                }
            }
            
            // 2. Interest
            BigDecimal remainingInterestToAllocate = quote.getOutstandingInterest();
            if (remainingInterestToAllocate.compareTo(BigDecimal.ZERO) > 0) {
                List<LoanInstallment> activeInstallments = schedule.getInstallments().stream()
                        .filter(i -> i.getStatus() == InstallmentStatus.OVERDUE || i.getStatus() == InstallmentStatus.PARTIALLY_PAID)
                        .sorted(Comparator.comparing(LoanInstallment::getInstallmentNumber))
                        .collect(Collectors.toList());
                        
                for (LoanInstallment inst : activeInstallments) {
                    if (remainingInterestToAllocate.compareTo(BigDecimal.ZERO) <= 0) break;
                    
                    BigDecimal interestAllocatedRepayment = repaymentAllocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
                    BigDecimal interestAllocatedPrepayment = prepaymentAllocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
                    
                    BigDecimal interestOutstanding = inst.getInterestAmount().subtract(interestAllocatedRepayment).subtract(interestAllocatedPrepayment);
                    if (interestOutstanding.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal toAllocate = remainingInterestToAllocate.min(interestOutstanding);
                        PrepaymentAllocation alloc = new PrepaymentAllocation(prepayment, inst, null, BigDecimal.ZERO, toAllocate, BigDecimal.ZERO);
                        allocations.add(prepaymentAllocationRepository.save(alloc));
                        remainingInterestToAllocate = remainingInterestToAllocate.subtract(toAllocate);
                    }
                }
            }
            
            // 3. Principal
            if (quote.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) > 0) {
                PrepaymentAllocation alloc = new PrepaymentAllocation(prepayment, null, null, quote.getOutstandingPrincipal(), BigDecimal.ZERO, BigDecimal.ZERO);
                allocations.add(prepaymentAllocationRepository.save(alloc));
            }

            prepaymentRepository.flush();
            String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String refNumber = String.format("PRE-%s-%06d", datePart, prepayment.getId());
            prepaymentRepository.updateTransactionReferenceNative(prepayment.getId(), refNumber);
            prepayment.setTransactionReference(refNumber);

            loan.setStatus(LoanApplicationStatus.CLOSED);
            
            notificationService.createNotification(
                 loan.getCustomer().getUser(),
                 NotificationType.LOAN_CLOSED,
                 "Loan Closed",
                 "Your loan application " + loan.getApplicationNumber() + " has been successfully closed following your full settlement of ₹" + requestedAmount + ".",
                 "PREPAYMENT",
                 prepayment.getId()
            );

            return mapToResponse(prepayment, allocations);
        }
    }

    private SettlementQuoteResponse calculateSettlement(LoanApplication loan, LoanAmortizationSchedule schedule) {
        // Outstanding Principal
        BigDecimal totalPrincipal = schedule.getTotalPrincipal();
        BigDecimal principalRepaid = repaymentAllocationRepository.sumPrincipalAllocatedByLoanApplicationId(loan.getId()).orElse(BigDecimal.ZERO);
        BigDecimal principalPrepaid = prepaymentAllocationRepository.sumPrincipalAllocatedByLoanApplication(loan).orElse(BigDecimal.ZERO);
        
        BigDecimal outstandingPrincipal = totalPrincipal.subtract(principalRepaid).subtract(principalPrepaid);
        if (outstandingPrincipal.compareTo(BigDecimal.ZERO) < 0) {
            outstandingPrincipal = BigDecimal.ZERO;
        }

        // Outstanding Interest (only OVERDUE and PARTIALLY_PAID)
        BigDecimal outstandingInterest = BigDecimal.ZERO;
        for (LoanInstallment inst : schedule.getInstallments()) {
            if (inst.getStatus() == InstallmentStatus.OVERDUE || inst.getStatus() == InstallmentStatus.PARTIALLY_PAID) {
                BigDecimal intRepaid = repaymentAllocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
                BigDecimal intPrepaid = prepaymentAllocationRepository.sumInterestAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
                BigDecimal remainingInt = inst.getInterestAmount().subtract(intRepaid).subtract(intPrepaid);
                if (remainingInt.compareTo(BigDecimal.ZERO) > 0) {
                    outstandingInterest = outstandingInterest.add(remainingInt);
                }
            }
        }

        // Active Penalties
        BigDecimal activePenaltiesOutstanding = BigDecimal.ZERO;
        List<LoanPenalty> activePenalties = penaltyRepository.findByLoanApplication(loan)
                .stream().filter(p -> p.getStatus() == PenaltyStatus.ACTIVE).collect(Collectors.toList());
                
        for (LoanPenalty penalty : activePenalties) {
            BigDecimal penPrepaid = prepaymentAllocationRepository.sumPenaltyAllocatedByPenalty(penalty).orElse(BigDecimal.ZERO);
            BigDecimal remainingPen = penalty.getPenaltyAmount().subtract(penPrepaid);
            if (remainingPen.compareTo(BigDecimal.ZERO) > 0) {
                activePenaltiesOutstanding = activePenaltiesOutstanding.add(remainingPen);
            }
        }
        
        BigDecimal settlementAmount = outstandingPrincipal.add(outstandingInterest).add(activePenaltiesOutstanding);
        
        return new SettlementQuoteResponse(outstandingPrincipal, outstandingInterest, activePenaltiesOutstanding, settlementAmount, LocalDateTime.now());
    }

    private LoanApplication getAuthorizedLoanApplication(Long applicationId, String userEmail) {
        LoanApplication loan = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanApplicationNotFoundException("Loan not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.CUSTOMER) {
            if (!loan.getCustomer().getUser().getId().equals(user.getId())) {
                throw new LoanApplicationNotFoundException("Loan not found");
            }
        }
        return loan;
    }

    private PrepaymentResponse mapToResponse(LoanPrepayment prepayment, List<PrepaymentAllocation> allocations) {
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPenalty = BigDecimal.ZERO;
        BigDecimal totalAllocated = BigDecimal.ZERO;
        
        for (PrepaymentAllocation alloc : allocations) {
            totalPrincipal = totalPrincipal.add(alloc.getPrincipalAllocated());
            totalInterest = totalInterest.add(alloc.getInterestAllocated());
            totalPenalty = totalPenalty.add(alloc.getPenaltyAllocated());
            totalAllocated = totalAllocated.add(alloc.getAllocatedAmount());
        }

        PrepaymentAllocationResponse allocationResponse = new PrepaymentAllocationResponse(totalPrincipal, totalInterest, totalPenalty, totalAllocated);

        return new PrepaymentResponse(
                prepayment.getTransactionReference(),
                prepayment.getAmount(),
                prepayment.getPrepaymentType(),
                prepayment.getPaidAt(),
                prepayment.getRecordedBy().getEmail(),
                allocationResponse
        );
    }
}
