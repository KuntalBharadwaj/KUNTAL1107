package com.cg.cred_metric.services;

import com.cg.cred_metric.models.*;
import com.cg.cred_metric.repositories.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreditScoreReportService {

    private final RepaymentRepository repaymentRepository;
    private final SuggestionRepository suggestionRepository;
    private final LoanRepository loanRepository;
    private final CreditCardRepository creditCardRepository;
    private final CreditScoreRepository creditScoreRepository;

    public byte[] generateMonthlyReport(User user, YearMonth month) throws Exception {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Repayment> allRepayments = repaymentRepository.findByUserAndPaymentDateBetween(user, start, end);
        CreditScoreSuggestion suggestion = suggestionRepository.findByUserAndSuggestionMonth(user, month);
        List<CreditCard> allCreditCard = creditCardRepository.findByUser(user);
        Optional<CreditScore> creditScore = creditScoreRepository.findByUser(user);

        List<Repayment> loanPayments = allRepayments.stream()
                .filter(r -> r.getRepaymentType() == Repayment.RepaymentType.LOAN)
                .toList();

        //Now for credit card

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
        Paragraph spacer = new Paragraph();
        spacer.setSpacingBefore(10f); // Adjust the spacing as needed (e.g., 3f, 5f, etc.)
        document.add(spacer);
        document.add(new Paragraph("User: " + user.getName(), normal));
        document.add(new Paragraph("Month: " + month.toString(), normal));
        document.add(Chunk.NEWLINE);

        // Loan Payments Table
        document.add(new Paragraph("Loan Repayment Summary", subHeader));
        document.add(spacer);
        document.add(generateRepaymentTable(loanPayments));

        document.add(Chunk.NEWLINE);

        // Credit Card Payments Table
        document.add(new Paragraph("Credit Card Report Summary", subHeader));
        document.add(spacer);
        document.add(generateRepaymentTable(creditCardPayments));

        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("All Credit Card Summary", subHeader));
        document.add(spacer);
        document.add(generateCreditCardTable(allCreditCard));

        // Credit Score Table
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("All Credit Score Summary", subHeader));
        document.add(spacer);
        document.add(generateCreditScoreTable(creditScore));
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
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{2, 2, 2, 2, 3});

        table.addCell(paddedCell(" Type ID ", Font.BOLD));
        table.addCell(paddedCell(" Payment Date ", Font.BOLD));
        table.addCell(paddedCell(" Status ", Font.BOLD));
        table.addCell(paddedCell(" Amount ", Font.BOLD));
        table.addCell(paddedCell(" Loan Type ", Font.BOLD));


        double totalPaid = 0.0;
        for (Repayment r : repayments) {
            String loanType = " Unknown ";
            if (r.getRepaymentTypeID() != null) {
                Loan loan = loanRepository.findById(r.getRepaymentTypeID()).orElse(null);
                if (loan != null) {
                    loanType = loan.getLoanType().toString();
                }
            }

            table.addCell(paddedCell(String.valueOf(r.getRepaymentTypeID()),Font.NORMAL));
            table.addCell(paddedCell(r.getPaymentDate().toString(), Font.NORMAL));
            table.addCell(paddedCell(r.getRepaymentStatus().name(), Font.NORMAL));
            table.addCell(paddedCell("Rs." + r.getAmountPaid(), Font.NORMAL));
            table.addCell(paddedCell(loanType, Font.NORMAL));

            totalPaid += r.getAmountPaid();
        }

        if (repayments.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No repayments available"));
            cell.setColspan(5);
            cell.setPadding(5f);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        } else {
            PdfPCell totalCell = new PdfPCell(new Phrase(" Total ", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            totalCell.setColspan(4);
            totalCell.setPadding(5f);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
            table.addCell(paddedCell("Rs." + totalPaid, Font.NORMAL));
        }

        return table;
    }

    private PdfPCell paddedCell(String text, int fontStyle) {
        Font font = new Font(Font.FontFamily.HELVETICA, 12, fontStyle);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5f);
        return cell;
    }

    private PdfPTable generateCreditCardTable(List<CreditCard> creditCards) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 3, 2, 2, 2, 2, 2}); // Add equal width for now

        // Headers
        table.addCell(paddedCell("Card Id", Font.BOLD));
        table.addCell(paddedCell("Card Limit", Font.BOLD));
        table.addCell(paddedCell("Bill Amount", Font.BOLD));
        table.addCell(paddedCell("Bill Due Date", Font.BOLD));
        table.addCell(paddedCell("Expiry Date", Font.BOLD));
        table.addCell(paddedCell("Status", Font.BOLD));
        table.addCell(paddedCell("Utilization %", Font.BOLD));

        if (creditCards.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase(" No credit cards available "));
            cell.setColspan(7); // ✅ Match the number of columns
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5f);
            table.addCell(cell);
            return table;
        }

        for (CreditCard card : creditCards) {
            table.addCell(paddedCell(card.getCardId().toString(), Font.NORMAL));
            table.addCell(paddedCell("Rs." + card.getCreditLimit(), Font.NORMAL));
            table.addCell(paddedCell("Rs." + card.getCardBillAmount(), Font.NORMAL));
            table.addCell(paddedCell(card.getBillDueDate().toString(), Font.NORMAL));
            table.addCell(paddedCell(card.getExpiryDate().toString(), Font.NORMAL));

            String status = card.getExpiryDate().isBefore(LocalDate.now()) ? " Expired " : " Active ";
            table.addCell(paddedCell(status, Font.ITALIC));

            // Utilization %

            BigDecimal billAmount = card.getCardBillAmount() != null ? BigDecimal.valueOf(card.getCardBillAmount()) : BigDecimal.ZERO;
            BigDecimal creditLimit = card.getCreditLimit() != null
                    ? card.getCreditLimit()
                    : BigDecimal.ONE; // to avoid division by zero


            BigDecimal utilization = BigDecimal.ZERO;
            if (creditLimit.compareTo(BigDecimal.ZERO) > 0) {
                utilization = billAmount.divide(creditLimit, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            table.addCell(paddedCell(String.format("%.2f%%", utilization), Font.NORMAL));
        }

        return table;
    }

    private PdfPTable generateCreditScoreTable(Optional<CreditScore> creditScore) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(new PdfPCell(new Phrase("Credit Score", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD))) {{ setPadding(5f); }});
        if (creditScore.isPresent()) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(creditScore.get().getScore()), new Font(Font.FontFamily.HELVETICA, 12))) {{ setPadding(5f); }});
        }
        else {
            table.addCell(new PdfPCell(new Phrase("No credit score available", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))) {{ setPadding(5f); }});
        }
        return table;
    }
}