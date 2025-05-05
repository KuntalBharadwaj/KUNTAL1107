package com.cg.cred_metric.dtos.Loan;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequestDTO {
    @NotBlank(message = "Loan type is required")
    @Pattern(regexp = "SECURED|UNSECURED", message = "Loan type must be SECURED or UNSECURED")
    private String loanType;

    @NotNull(message = "Principal amount is required")
    @Positive(message = "Principal amount must be positive")
    private Double principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Interest rate must be greater than 0")
    @DecimalMax(value = "100.0", message = "Interest rate cannot be more than 100%")
    private Double interestRate;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Future(message = "End date must be in the future, You must enter the loans that are still active")
    private LocalDate endDate;

    @NotNull(message = "EMI amount is required")
    @Positive(message = "EMI amount must be positive")
    private Double emiAmount;

    @NotNull(message = "EMI due date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate emiDueDate;
}
