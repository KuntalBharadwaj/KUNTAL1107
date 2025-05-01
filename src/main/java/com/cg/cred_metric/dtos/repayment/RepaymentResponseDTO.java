package com.cg.cred_metric.dtos.repayment;

import com.cg.cred_metric.models.Repayment;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RepaymentResponseDTO {
<<<<<<< HEAD
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
=======
    private Repayment.RepaymentType repaymentType;
    private Long repaymentTypeID;
    private LocalDate paymentDate;
    private Repayment.RepaymentStatus repaymentStatus;
    private Double amountPaid;

    public RepaymentResponseDTO(Repayment repayment) {
        this.repaymentType = repayment.getRepaymentType();
        this.repaymentTypeID = repayment.getRepaymentTypeID();
        this.paymentDate = repayment.getPaymentDate();
        this.repaymentStatus = repayment.getRepaymentStatus();
>>>>>>> naman
        this.amountPaid = repayment.getAmountPaid();
    }
}
