package com.cg.cred_metric.Schedulers;


import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.services.CreditScoreReportService;
import com.cg.cred_metric.utils.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
public class CreditReportSummaryScheduler {

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private CreditScoreReportService creditScoreReportService;

    @Autowired
    private MailService mailService;

    // Scheduled to run at the beginning of each month
    //@Scheduled(cron = "0 0 0 1 * ?")
    @Scheduled(cron = "0 * * * * ?")

    public void generateAndSendReports() {
        log.info("Starting monthly credit report summary generation...");

        List<User> allUsers = userRespository.findAll();
        YearMonth lastMonth = YearMonth.now().minusMonths(1);

        for (User user : allUsers) {
            try {
                log.info("Generating report for user ID: {}", user.getUserId());

                byte[] report = creditScoreReportService.generateMonthlyReport(user, lastMonth);

                if (report != null && report.length > 0) {
                    String subject = "Your Credit Report Summary for " + lastMonth.getMonth() + " " + lastMonth.getYear();
                    String body = "Dear " + user.getName() + ",\n\nPlease find attached your credit report summary for " + lastMonth.getMonth() + " " + lastMonth.getYear() + ".\n\nRegards,\nCredit Metrics Team";

                    mailService.sendEmailWithAttachment(user.getEmail(), subject, body, report, "CreditReport_" + lastMonth + ".pdf");

                    log.info("Report sent to user ID: {}", user.getUserId());
                } else {
                    log.warn("Empty report generated for user ID: {}", user.getUserId());
                }

            } catch (Exception e) {
                log.error("Failed to generate/send report for user ID: {}", user.getUserId(), e);
            }
        }

        log.info("Monthly credit report summary generation completed.");
    }
}

