package com.cg.cred_metric.dtos.Loan;

import com.cg.cred_metric.models.Loan;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanResponseDTO {
    private Long id;
    private String loanType;
    private Double principalAmount;
    private Double interestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double emiAmount;
    private LocalDate emiDueDate;
    private String status;


    public LoanResponseDTO(Loan loan) {
        this.id = loan.getLoanId();
        this.loanType = String.valueOf(loan.getLoanType());
        this.principalAmount = loan.getPrincipalAmount();
        this.interestRate = loan.getInterestRate();
        this.startDate = loan.getStartDate();
        this.endDate = loan.getEndDate();
        this.emiAmount = loan.getEmiAmount();
        this.emiDueDate = loan.getEmiDueDate();
        this.status = loan.getStatus().toString();
    }
}
