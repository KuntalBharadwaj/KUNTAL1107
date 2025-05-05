package com.cg.cred_metric.dtos.CreditCard;

import com.cg.cred_metric.models.CreditCard;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreditCardResponseDTO {

    private Long cardId;
    private BigDecimal creditLimit;
    private BigDecimal currentBalance;
    private LocalDate issueDate;
    private LocalDate expiryDate;

    // Constructor to create CreditCardResponseDTO from CreditCardDTO
    public CreditCardResponseDTO(CreditCard creditCard) {
        this.cardId = creditCard.getCardId();
        this.creditLimit = creditCard.getCreditLimit();
        this.currentBalance = creditCard.getCurrentBalance();
        this.issueDate = creditCard.getIssueDate();
        this.expiryDate = creditCard.getExpiryDate();
    }
}

