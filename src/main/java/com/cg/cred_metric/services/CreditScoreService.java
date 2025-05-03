package com.cg.cred_metric.services;

import com.cg.cred_metric.models.*;
import com.cg.cred_metric.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final UserRespository userRepository;
    private final LoanRepository loanRepository;
    private final RepaymentRepository repaymentRepository;
    private final CreditCardRepository creditCardRepository;
    private final CreditScoreRepository creditScoreRepository;



    public int calculateScoreForUser(Long userId) {
        // Fetch user from DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch related data
        List<Loan> loans = loanRepository.findByUser(user);
        List<Repayment> repayments = repaymentRepository.findByUser(user);
        List<CreditCard> cards = creditCardRepository.findByUser(user);

        System.out.println(loans.size());
        System.out.println(repayments.size());
        System.out.println(cards.size());

        double baseScore = 750;

        // -----------------------------
        // 1. Payment History (35%)
        // -----------------------------
        long totalRepayments = repayments.size();
        long onTimeRepayments = repayments.stream()
                .filter(r -> r.getRepaymentStatus() == Repayment.RepaymentStatus.ONTIME)
                .count();

        double paymentPenalty = 0;
        if (totalRepayments > 0) {
            double missedRatio = 1 - ((double) onTimeRepayments / totalRepayments);
            // Max penalty: -50
            paymentPenalty = -1 * missedRatio * 50;
        }

        // -----------------------------
        // 2. Credit Utilization (30%)
        // -----------------------------
        BigDecimal totalLimit = BigDecimal.ZERO;
        BigDecimal totalUsed = BigDecimal.ZERO;

        for (CreditCard card : cards) {
            if (card.getCreditLimit() != null) {
                totalLimit = totalLimit.add(card.getCreditLimit());
            }
            if (card.getCurrentBalance() != null) {
                totalUsed = totalUsed.add(card.getCurrentBalance());
            }
        }

        double utilizationPenalty = 0;
        if (totalLimit.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = totalUsed.divide(totalLimit, 2, RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.3")) > 0) {
                // Max penalty: -30
                utilizationPenalty = -30;
            }
        }

        // -----------------------------
        // 3. Credit Mix (10%)
        // -----------------------------
        long secured = loans.stream().filter(l -> l.getLoanType() == Loan.LoanType.SECURED).count();
        long unsecured = loans.stream().filter(l -> l.getLoanType() == Loan.LoanType.UNSECURED).count();

        double creditMixBonus = (secured > 0 && unsecured > 0) ? 10 : 0; // +10 points

        // -----------------------------
        // 4. New Credit Enquiries (10%)
        // -----------------------------
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        long recentLoans = loanRepository.countByUserAndStartDateAfter(user, sixMonthsAgo);

        double enquiryPenalty = recentLoans > 2 ? -20 : 0; // -30 points (10% of 150)

        // -----------------------------
        // 5. Credit History Length (15%)
        // -----------------------------
        LocalDate oldestLoanDate = loans.stream()
                .map(Loan::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate oldestCardDate = cards.stream()
                .map(CreditCard::getIssueDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate oldestCredit = oldestLoanDate.isBefore(oldestCardDate) ? oldestLoanDate : oldestCardDate;
        long months = ChronoUnit.MONTHS.between(oldestCredit, LocalDate.now());

        double historyBonus = months >= 12 ? 10 : 0; // +45 points (15% of 150)

        // -----------------------------
        // Final Score Calculation
        // -----------------------------
        double finalScore = baseScore
                + paymentPenalty
                + utilizationPenalty
                + creditMixBonus
                + enquiryPenalty
                + historyBonus;

        // Clamp between 300–900
        int score = Math.min(Math.max((int) Math.round(finalScore), 300), 900);

        // Save to DB
        CreditScore cs = creditScoreRepository.findByUser(user)
                .orElse(new CreditScore());
        cs.setUser(user);
        cs.setScore(score);
        creditScoreRepository.save(cs);

        return score;
    }

    // Monthly Scheduler
    @Scheduled(cron = "0 0 2 1 * *") // 2:00 AM on the 1st of every month
    @Transactional
    public void scheduleMonthlyCreditScoreCalculation() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            int score = calculateScoreForUser(user.getUserId());
        }
    }
}
