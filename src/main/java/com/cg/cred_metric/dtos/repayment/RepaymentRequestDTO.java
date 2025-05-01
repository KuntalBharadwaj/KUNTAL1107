package com.cg.cred_metric.dtos.repayment;

import com.cg.cred_metric.models.Repayment;
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

    @NotNull(message = "Repayment type is required")
    private Repayment.RepaymentType repaymentType;

    @NotNull(message = "Repayment type ID is required") // Loan ID or Credit Card ID
    private Long repaymentTypeID;

    @NotNull(message = "Payment date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @NotNull(message = "Repayment amount paid is required")
    private Double amountPaid;
}