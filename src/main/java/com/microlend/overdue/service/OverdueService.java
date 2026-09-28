package com.microlend.overdue.service;

import com.microlend.amortization.entity.InstallmentStatus;
import com.microlend.amortization.entity.LoanInstallment;
import com.microlend.amortization.repository.LoanInstallmentRepository;
import com.microlend.disbursement.entity.DisbursementStatus;
import com.microlend.overdue.dto.OverdueProcessingResponse;
import com.microlend.overdue.entity.LoanPenalty;
import com.microlend.overdue.repository.LoanPenaltyRepository;
import com.microlend.repayment.repository.RepaymentAllocationRepository;
import com.microlend.notification.service.NotificationService;
import com.microlend.notification.entity.NotificationType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
public class OverdueService {

    private final LoanInstallmentRepository installmentRepository;
    private final LoanPenaltyRepository penaltyRepository;
    private final RepaymentAllocationRepository allocationRepository;
    private final Clock clock;
    private final NotificationService notificationService;

    private static final BigDecimal PENALTY_RATE = new BigDecimal("0.001");

    public OverdueService(LoanInstallmentRepository installmentRepository,
                          LoanPenaltyRepository penaltyRepository,
                          RepaymentAllocationRepository allocationRepository,
                          Clock clock,
                          NotificationService notificationService) {
        this.installmentRepository = installmentRepository;
        this.penaltyRepository = penaltyRepository;
        this.allocationRepository = allocationRepository;
        this.clock = clock;
        this.notificationService = notificationService;
    }

    @Transactional
    public OverdueProcessingResponse processOverdueInstallments() {
        LocalDate businessDate = LocalDate.now(clock);
        LocalDateTime processedAt = LocalDateTime.now(clock);

        List<InstallmentStatus> activeStatuses = Arrays.asList(InstallmentStatus.PENDING, InstallmentStatus.PARTIALLY_PAID);

        List<LoanInstallment> overdueInstallments = installmentRepository.findOverdueInstallments(
                DisbursementStatus.DISBURSED,
                activeStatuses,
                businessDate
        );

        int processedInstallments = 0;
        int newPenaltiesCreated = 0;
        int alreadyProcessed = 0;
        BigDecimal totalPenaltyCreated = BigDecimal.ZERO;

        for (LoanInstallment inst : overdueInstallments) {
            processedInstallments++;

            BigDecimal principalAllocated = allocationRepository.sumPrincipalAllocatedByInstallment(inst).orElse(BigDecimal.ZERO);
            BigDecimal outstandingPrincipal = inst.getPrincipalAmount().subtract(principalAllocated);

            if (outstandingPrincipal.compareTo(BigDecimal.ZERO) <= 0) {
                // If by some anomaly it's fully paid principal but marked partially paid (maybe interest outstanding),
                // we only charge penalty on outstanding principal.
                continue;
            }

            int daysOverdue = (int) ChronoUnit.DAYS.between(inst.getDueDate(), businessDate);
            if (daysOverdue <= 0) {
                continue;
            }

            // Mark as OVERDUE
            if (inst.getStatus() != InstallmentStatus.OVERDUE) {
                inst.setStatus(InstallmentStatus.OVERDUE);
                installmentRepository.save(inst);
                
                notificationService.createNotification(
                    inst.getSchedule().getLoanApplication().getCustomer().getUser(),
                    NotificationType.INSTALLMENT_OVERDUE,
                    "Installment Overdue",
                    "Your installment #" + inst.getInstallmentNumber() + " is now overdue.",
                    "INSTALLMENT",
                    inst.getId()
                );
            }

            // Check Idempotency for this day
            if (penaltyRepository.existsByInstallmentAndPenaltyDate(inst, businessDate)) {
                alreadyProcessed++;
                continue;
            }

            BigDecimal dailyPenalty = outstandingPrincipal.multiply(PENALTY_RATE).setScale(2, RoundingMode.HALF_UP);
            
            LoanPenalty penalty = new LoanPenalty(
                    inst.getSchedule().getLoanApplication(),
                    inst,
                    businessDate,
                    dailyPenalty,
                    PENALTY_RATE,
                    daysOverdue,
                    processedAt
            );

            try {
                penalty = penaltyRepository.saveAndFlush(penalty);
                
                notificationService.createNotification(
                    inst.getSchedule().getLoanApplication().getCustomer().getUser(),
                    NotificationType.PENALTY_GENERATED,
                    "Penalty Generated",
                    "A late penalty of ₹" + dailyPenalty + " has been added to your overdue installment #" + inst.getInstallmentNumber() + ".",
                    "PENALTY",
                    penalty.getId()
                );
                
                newPenaltiesCreated++;
                totalPenaltyCreated = totalPenaltyCreated.add(dailyPenalty);
            } catch (DataIntegrityViolationException e) {
                // Concurrent insert happened. Log and continue
                alreadyProcessed++;
            }
        }

        return new OverdueProcessingResponse(
                processedInstallments,
                newPenaltiesCreated,
                alreadyProcessed,
                totalPenaltyCreated,
                processedAt
        );
    }
}
