package com.cg.cred_metric.dtos.CreditCard;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreditCardDTO {

    @NotNull(message = "Credit Limit cannot be null")
    @Positive(message = "Credit Limit must be positive")
    private BigDecimal creditLimit;

    @NotNull(message = "Current Balance cannot be null")
    @Positive(message = "Current Balance must be positive")
    private BigDecimal currentBalance;

    @NotNull(message = "Issue Date cannot be null")
    private LocalDate issueDate;

    @NotNull(message = "Expiry Date cannot be null")
    private LocalDate expiryDate;

    @NotNull(message = "Card Bill Amount cannot be null")
    private Double cardBillAmount;

    @NotNull(message = "Bill Due Date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDueDate;
}