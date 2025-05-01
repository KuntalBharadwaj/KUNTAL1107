package com.cg.cred_metric.dtos.repayment;

import com.cg.cred_metric.models.Repayment;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RepaymentResponseDTO {
    private String repaymentType;
    private Long repaymentTypeID;
    private LocalDate paymentDate;
    private String repaymentStatus;
    private Double amountPaid;

    public RepaymentResponseDTO(Repayment repayment) {
        this.repaymentType = String.valueOf(repayment.getRepaymentType());
        this.repaymentTypeID = repayment.getRepaymentTypeID();
        this.paymentDate = repayment.getPaymentDate();
        this.repaymentStatus = String.valueOf(repayment.getRepaymentStatus());
        this.amountPaid = repayment.getAmountPaid();
    }
}
