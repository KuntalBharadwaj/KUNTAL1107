package com.cg.cred_metric.Schedulers;

import com.cg.cred_metric.models.*;
import com.cg.cred_metric.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    // Scheduled to run every 2 minutes for testing
    // @Scheduled(cron = "0 0/2 * * * ?")
    @Scheduled(cron = "0 0 0 1 * ?")
    public void generateMonthlySuggestions() {
        log.info("Starting the suggestion generation process...");

        List<User> allUsers = userRespository.findAll();

        // Pichle mahine ka date range nikalo
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate startDate = lastMonth.atDay(1);
        LocalDate endDate = lastMonth.atEndOfMonth();

        for (User user : allUsers) {
            log.info("Processing suggestions for user ID: {}", user.getUserId());

            List<String> suggestionsList = new ArrayList<>();

            // Sab loans nikalo is user ke
            List<Loan> userLoans = loanRepository.findByUser(user);

            for (Loan loan : userLoans) {
                log.info("Checking missed repayments for Loan ID: {}", loan.getLoanId());

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
                    // suggestionsList.add("You missed " + missedCount + " EMI(s) for Loan ID" + loan.getLoanId());
                    suggestionsList.add("Pay, Your EMI(s) on time.");
                }
            }

            // Agar koi suggestion bani ho to save karo
            if (!suggestionsList.isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String suggestionsJson = objectMapper.writeValueAsString(suggestionsList);

                    CreditScoreSuggestion suggestion = new CreditScoreSuggestion();
                    suggestion.setUser(user);
                    suggestion.setSuggestionMonth(lastMonth);
                    suggestion.setSuggestions(suggestionsJson);

                    log.info("Suggestions JSON: {}", suggestionsJson);

                    // Save the suggestion to the repository
                    suggestionRepository.save(suggestion);
                    log.info("Suggestion saved for user ID: {}", user.getUserId());

                } catch (Exception e) {
                    log.error("Error occurred while saving suggestion for user ID: {}", user.getUserId(), e);
                }
            } else {
                log.info("No suggestions for user ID: {}", user.getUserId());
            }
        }

        log.info("Suggestion generation process completed.");
    }
}
