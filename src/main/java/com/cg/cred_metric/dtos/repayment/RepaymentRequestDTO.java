package com.cg.cred_metric.dtos.repayment;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentRequestDTO {
    @NotBlank(message = "Repayment type is required")
    @Pattern(regexp = "CREDIT_CARD|LOAN", message = "Repayment type must be CREDIT_CARD or LOAN")
    private String repaymentType;

    @NotNull(message = "Repayment type ID is required") // Loan ID or Credit Card ID
    private Long repaymentTypeID;

    @NotNull(message = "Payment date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @NotBlank(message = "Repayment status is required")
    @Pattern(regexp = "MISSED|PENDING|ONTIME", message = "Repayment status must be MISSED or PENDING or ONTIME")
    private String repaymentStatus;

    @NotNull(message = "Repayment amount paid is required")
    private Double amountPaid;
}