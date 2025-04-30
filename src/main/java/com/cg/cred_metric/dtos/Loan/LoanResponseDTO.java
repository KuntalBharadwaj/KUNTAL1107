package com.cg.cred_metric.dtos.Loan;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanResponseDTO {
    private String loanType;
    private Double principalAmount;
    private Double interestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double emiAmount;
    private LocalDate emiDueDate;
    private String status;
}
