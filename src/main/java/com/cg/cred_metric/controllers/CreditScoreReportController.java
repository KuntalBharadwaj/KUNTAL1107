package com.cg.cred_metric.controllers;

import com.cg.cred_metric.models.User;
import com.cg.cred_metric.services.CreditScoreReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class CreditScoreReportController {

    private final CreditScoreReportService reportService;

    @GetMapping("/summary")
    public void downloadReport(
            @AuthenticationPrincipal User user,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            HttpServletResponse response
    ) {
        try {
            byte[] pdfData = reportService.generateMonthlyReport(user, month);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Account_Summary_" + month + ".pdf");
            response.getOutputStream().write(pdfData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

