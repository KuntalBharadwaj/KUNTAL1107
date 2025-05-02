package com.cg.cred_metric.Schedulers;

import com.cg.cred_metric.models.*;
import com.cg.cred_metric.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SuggestionScheduler {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private RepaymentRepository repaymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private ObjectMapper objectMapper;

    // Scheduled to run every 2 minutes for testing
    //@Scheduled(cron = "0 0/1 * * * ?")
    // Scheduled to run at the beginning of each month
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlySuggestions() {
        log.info("Starting the suggestion generation process...");

        List<User> allUsers = userRespository.findAll();
        YearMonth lastMonth = YearMonth.now().minusMonths(1);

        log.info("Last month: " + lastMonth);

        LocalDate startDate = lastMonth.atDay(1);
        LocalDate endDate = lastMonth.atEndOfMonth();

        for (User user : allUsers) {
            log.info("Processing suggestions for user ID: {}", user.getUserId());
            List<String> suggestionsList = new ArrayList<>();

            // Process Loan Suggestions
            processLoanSuggestions(user, suggestionsList, startDate, endDate);

            // Process Credit Card Suggestions
            processCreditCardSuggestions(user, suggestionsList, startDate, endDate);

            // Save Suggestions
            saveSuggestions(user, suggestionsList, lastMonth);
        }
        log.info("Suggestion generation process completed.");
    }

    private void processLoanSuggestions(User user, List<String> suggestionsList, LocalDate startDate, LocalDate endDate) {
        List<Loan> userLoans = loanRepository.findByUser(user);

        for (Loan loan : userLoans) {
            log.info("Checking missed repayments for Loan ID: {}", user.getUserId());

            long missedCount = repaymentRepository
                    .countByUserAndRepaymentTypeAndRepaymentTypeIDAndRepaymentStatusAndPaymentDateBetween(
                            user,
                            Repayment.RepaymentType.LOAN,
                            loan.getLoanId(),
                            Repayment.RepaymentStatus.MISSED,
                            startDate,
                            endDate
                    );
            log.info("Missed count for Loan ID {}: {}", loan.getLoanId(), missedCount);

            if (missedCount > 0) {
                suggestionsList.add("Pay, Your EMI(s) on time to avoid late fees and negative credit score impact.");
            }
        }
    }

    private void processCreditCardSuggestions(User user, List<String> suggestionsList, LocalDate startDate, LocalDate endDate) {
        List<CreditCard> userCreditCards = creditCardRepository.findByUser(user);

        for (CreditCard card : userCreditCards) {
            log.info("Checking missed repayments for Card ID: {}", user.getUserId());

            long missedCount = repaymentRepository.countByUserAndRepaymentTypeAndRepaymentTypeIDAndRepaymentStatusAndPaymentDateBetween(
                    user,
                    Repayment.RepaymentType.CREDIT_CARD,
                    card.getCardId(),
                    Repayment.RepaymentStatus.MISSED,
                    startDate,
                    endDate
            );
            log.info("Missed count for Card ID {}: {}", card.getCardId(), missedCount);

            if (missedCount > 0) {
                suggestionsList.add("Pay your credit card bill(s) on time to avoid late fees and high interest charges.");
            }

            // Check Credit Card Utilization Ratio
            BigDecimal currentBalance = card.getCurrentBalance();
            BigDecimal creditLimit = card.getCreditLimit();
            if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) > 0 && currentBalance != null) {
                BigDecimal utilizationRatio = currentBalance.divide(creditLimit, 2, BigDecimal.ROUND_HALF_UP); // Calculate to 2 decimal places
                if (utilizationRatio.compareTo(new BigDecimal("0.30")) > 0) {
                    suggestionsList.add("Keep your credit utilization ratio below 30% to maintain a good credit score. Your current utilization is " + utilizationRatio.multiply(new BigDecimal("100")) + "%");
                }
            }
        }
    }

    private void saveSuggestions(User user, List<String> suggestionsList, YearMonth lastMonth) {
        if (!suggestionsList.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String suggestionsJson = objectMapper.writeValueAsString(suggestionsList);

                log.info(suggestionsJson);

                CreditScoreSuggestion suggestion = new CreditScoreSuggestion();
                suggestion.setUser(user);
                suggestion.setSuggestionMonth(lastMonth);
                suggestion.setSuggestions(suggestionsJson);
                log.info("Suggestions JSON: {}", suggestionsJson);

                suggestionRepository.save(suggestion);
                log.info("Saved suggestions for user ID: {}", user.getUserId());
            }
            catch (Exception e) {
                log.error("Error saving suggestions for user ID: {}", user.getUserId(), e);
            }
        }
        else {
            log.info("No suggessions found for user ID: {}", user.getUserId());
        }
    }
}
