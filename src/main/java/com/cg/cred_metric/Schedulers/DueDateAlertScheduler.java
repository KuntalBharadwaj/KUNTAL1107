package com.cg.cred_metric.Schedulers;

import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.models.CreditCard;
import com.cg.cred_metric.models.Loan;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.CreditCardRepository;
import com.cg.cred_metric.repositories.LoanRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class DueDateAlertScheduler {
    @Autowired
    private UserRespository userRespository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private MailService mailService;

    private static final int ALERT_INTERVAL_DAYS = 7;

    // Runs every day at 1 am
    // @Scheduled(cron = "0 0 1 * * ?")
    // For testing runs after every minute
    @Scheduled(cron = "1 * * * * *")
    public void sendDueDateAlert() {
        log.info("Starting Due Date Alert Scheduler...");
        LocalDate today = LocalDate.now();
        LocalDate alertDate = today.plusDays(ALERT_INTERVAL_DAYS);

        log.info("Today's date: {}, Alert date: {}", today, alertDate);

        sendLoanEmiReminder(today, alertDate);
        sendCreditCardBillReminder(today, alertDate);

        log.info("Finished Sending Due Date Alerts...");
    }

    private void sendLoanEmiReminder(LocalDate today, LocalDate alertDate) {
        log.info("Sending Loan EMI reminders...");
        List<Loan> loanEmiDueDate = loanRepository.findByEmiDueDateBetweenAndReminderSentFalse(today, alertDate);

        for (Loan loan : loanEmiDueDate) {
            User user = userRespository.findById(loan.getUser().getUserId())
                            .orElse(null);
            if (user != null) {
                String email = user.getEmail();
                String subject = "Loan Emi Due Date Reminder";
                String body = String.format(
                        "Dear %s,\n\nYour EMI for Loan ID %d is due on %s.  Please ensure timely payment.\n\nLoan Details:\nLoan ID: %d\nPrincipal Amount: %.2f\nInterest Rate: %.2f%%\nEMI Amount: %.2f\nEMI Due Date: %s\n\nWarm Regards,\nTeam Cred Metric",
                        user.getName(), loan.getLoanId(), loan.getEmiDueDate().toString(), loan.getLoanId(), loan.getPrincipalAmount(), loan.getInterestRate(), loan.getEmiAmount(), loan.getEmiDueDate()
                );

                log.info("Sending email to: {}, Subject: {}", email, subject);
                mailService.sendMail(email, subject, body);
                loan.setReminderSent(true);
                loanRepository.save(loan);
                log.info("Reminder sent to: {}", email);
            }
            else {
                throw new ResourceNotFoundException("User not found for Loan ID: " + loan.getLoanId());
            }
        }
        log.info("Finished sending Loan EMI Reminders...");
    }

    private void sendCreditCardBillReminder(LocalDate today, LocalDate alertDate) {
        log.info("Sending Credit Card Bill reminders...");
        List<CreditCard> cardBillDueDate = creditCardRepository.findByBillDueDateBetweenAndReminderSentFalse(today, alertDate);

        for (CreditCard card : cardBillDueDate) {
            User user = userRespository.findById(card.getUser().getUserId())
                    .orElse(null);
            if (user != null) {
                String email = user.getEmail();
                String subject = "Credit Card Bill Due Date Reminder";
                String body = String.format(
                        "Dear %s,\n\nYour Credit Card bill for Credit Card ID %d is due on %s.  Please ensure timely payment.\n\nCredit Card Details:\nCard ID: %d\nCredit Limit: ₹%.2f\nCurrent Balance: ₹%.2f\nCard Bill Amount: ₹%.2f\nCard Bill Due Date: %s\n\nWarm Regards,\nTeam Cred Metric",
                        user.getName(), card.getCardId(), card.getBillDueDate().toString(), card.getCardId(), card.getCreditLimit(), card.getCurrentBalance(), card.getCardBillAmount(), card.getBillDueDate()
                );

                log.info("Sending email to: {}, Subject: {}", email, subject);
                mailService.sendMail(email, subject, body);
                card.setReminderSent(true);
                creditCardRepository.save(card);
                log.info("Reminder sent to: {}", email);
            }
            else {
                throw new ResourceNotFoundException("User not found for Credit Card ID: " + card.getCardId());
            }
        }
        log.info("Finished sending Credit Card Bill Reminders...");
    }
}