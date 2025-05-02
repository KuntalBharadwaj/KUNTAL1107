package com.cg.cred_metric.services;

//import com.cg.cred_metric.models.CreditScoreSuggestion;
//import com.cg.cred_metric.models.Repayment;
//import com.cg.cred_metric.models.User;
//import com.cg.cred_metric.repositories.RepaymentRepository;
//import com.cg.cred_metric.repositories.SuggestionRepository;
//import com.itextpdf.text.*;
//import com.itextpdf.text.pdf.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class CreditScoreReportService {
//
//    private final RepaymentRepository repaymentRepository;
//    private final SuggestionRepository suggestionRepository;
//
//    public byte[] generateMonthlyReport(User user, YearMonth month) throws Exception {
//        LocalDate start = month.atDay(1);
//        LocalDate end = month.atEndOfMonth();
//
//        List<Repayment> allRepayments = repaymentRepository.findByUserAndPaymentDateBetween(user, start, end);
//        CreditScoreSuggestion suggestion = suggestionRepository.findByUserAndSuggestionMonth(user, month);
//
//        List<Repayment> loanPayments = allRepayments.stream()
//                .filter(r -> r.getRepaymentType() == Repayment.RepaymentType.LOAN)
//                .toList();
//
//        Document document = new Document();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        PdfWriter.getInstance(document, out);
//        document.open();
//
//        Font header = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
//        Font subHeader = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
//        Font normal = new Font(Font.FontFamily.HELVETICA, 12);
//
//        document.add(new Paragraph("Credit Score Analyzer Report", header));
//        document.add(new Paragraph("User: " + user.getName(), normal));
//        document.add(new Paragraph("Month: " + month.toString(), normal));
//        document.add(Chunk.NEWLINE);
//
//        // Loan Payments Table
//        document.add(new Paragraph("Loan Repayment Summary", subHeader));
//        document.add(generateRepaymentTable(loanPayments));
//
//        document.add(Chunk.NEWLINE);
//
//        // Suggestions
//        document.add(new Paragraph("Suggestions", subHeader));
//        if (suggestion != null) {
//            document.add(new Paragraph(suggestion.getSuggestions(), normal));
//        } else {
//            document.add(new Paragraph("No suggestions available for this month.", normal));
//        }
//
//        document.close();
//        return out.toByteArray();
//    }
//
//    private PdfPTable generateRepaymentTable(List<Repayment> repayments) throws DocumentException {
//        PdfPTable table = new PdfPTable(4);
//        table.setWidthPercentage(100);
//        table.setWidths(new int[]{2, 2, 2, 2});
//
//        table.addCell("Type ID");
//        table.addCell("Payment Date");
//        table.addCell("Status");
//        table.addCell("Amount");
//        table.addCell("Loan Type");
//
//        double totalPaid = 0.0;
//        for (Repayment r : repayments) {
//            table.addCell(String.valueOf(r.getRepaymentTypeID()));
//            table.addCell(r.getPaymentDate().toString());
//            table.addCell(r.getRepaymentStatus().name());
//            table.addCell("Rs." + r.getAmountPaid());
//            totalPaid += r.getAmountPaid();
//        }
//
//        if (repayments.isEmpty()) {
//            PdfPCell cell = new PdfPCell(new Phrase("No loan repayments available"));
//            cell.setColspan(4);
//            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//            table.addCell(cell);
//        } else {
//            PdfPCell totalCell = new PdfPCell(new Phrase("Total"));
//            totalCell.setColspan(3);
//            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//            table.addCell(totalCell);
//            table.addCell("Rs." + totalPaid);
//        }
//
//        return table;
//    }
//}

import com.cg.cred_metric.models.*;
import com.cg.cred_metric.repositories.CreditCardRepository;
import com.cg.cred_metric.repositories.LoanRepository;
import com.cg.cred_metric.repositories.RepaymentRepository;
import com.cg.cred_metric.repositories.SuggestionRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditScoreReportService {

    private final RepaymentRepository repaymentRepository;
    private final SuggestionRepository suggestionRepository;
    private final LoanRepository loanRepository; // ✅ Inject LoanRepository
    private final CreditCardRepository creditCardRepository;

    public byte[] generateMonthlyReport(User user, YearMonth month) throws Exception {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Repayment> allRepayments = repaymentRepository.findByUserAndPaymentDateBetween(user, start, end);
        CreditScoreSuggestion suggestion = suggestionRepository.findByUserAndSuggestionMonth(user, month);
        List<CreditCard> allCreditCard = creditCardRepository.findByUser(user);


        List<Repayment> loanPayments = allRepayments.stream()
                .filter(r -> r.getRepaymentType() == Repayment.RepaymentType.LOAN)
                .toList();

        //Now for creadit card

        List<Repayment> creditCardPayments = allRepayments.stream()
                .filter(r -> r.getRepaymentType() == Repayment.RepaymentType.CREDIT_CARD)
                .toList();

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();


        Font header = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font subHeader = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 12);

        document.add(new Paragraph("Credit Score Analyzer Report", header));
        document.add(new Paragraph("User: " + user.getName(), normal));
        document.add(new Paragraph("Month: " + month.toString(), normal));
        document.add(Chunk.NEWLINE);

        // Loan Payments Table
        document.add(new Paragraph("Loan Repayment Summary", subHeader));
        document.add(generateRepaymentTable(loanPayments));

        document.add(Chunk.NEWLINE);

        // Credit Card Payments Table
        document.add(new Paragraph("Credit Card Report Summary", subHeader));
        document.add(generateRepaymentTable(creditCardPayments));

        document.add(Chunk.NEWLINE);
        // Suggestions
        document.add(new Paragraph("Suggestions", subHeader));
        if (suggestion != null) {
            document.add(new Paragraph(suggestion.getSuggestions(), normal));
        } else {
            document.add(new Paragraph("No suggestions available for this month.", normal));
        }

        document.close();
        return out.toByteArray();
    }

    private PdfPTable generateRepaymentTable(List<Repayment> repayments) throws DocumentException {
        PdfPTable table = new PdfPTable(5); // ✅ Now 5 columns
        table.setWidthPercentage(100);
        table.setWidths(new int[]{2, 2, 2, 2, 3});

        table.addCell("Type ID");
        table.addCell("Payment Date");
        table.addCell("Status");
        table.addCell("Amount");
        table.addCell("Loan Type"); // ✅ Header for loan type

        double totalPaid = 0.0;
        for (Repayment r : repayments) {
            String loanType = "Unknown";
            if (r.getRepaymentTypeID() != null) {
                Loan loan = loanRepository.findById(r.getRepaymentTypeID()).orElse(null);
                if (loan != null) {
                    loanType = loan.getLoanType().toString();
                }
            }

            table.addCell(String.valueOf(r.getRepaymentTypeID()));
            table.addCell(r.getPaymentDate().toString());
            table.addCell(r.getRepaymentStatus().name());
            table.addCell("Rs." + r.getAmountPaid());
            table.addCell(loanType); // ✅ Insert loan type
            totalPaid += r.getAmountPaid();
        }

        if (repayments.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No loan repayments available"));
            cell.setColspan(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        } else {
            PdfPCell totalCell = new PdfPCell(new Phrase("Total"));
            totalCell.setColspan(4);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
            table.addCell("Rs." + totalPaid);
        }

        return table;
    }
}


