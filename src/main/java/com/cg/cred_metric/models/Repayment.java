package com.cg.cred_metric.models;

import com.cg.cred_metric.dtos.repayment.RepaymentRequestDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "repayments")
@NoArgsConstructor
@Data
public class Repayment {
    @Id
    @GeneratedValue
    private Long repaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    private RepaymentType repaymentType; // CREDIT_CARD or LOAN

    private Long repaymentTypeID;

    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private RepaymentStatus repaymentStatus; // MISSED or PENDING or ONTIME

    private Double amountPaid;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RepaymentType {
        CREDIT_CARD,
        LOAN
    }

    public enum RepaymentStatus {
        MISSED,
        PENDING,
        ONTIME
    }

    public Repayment(RepaymentRequestDTO repaymentRequestDTO) {
        this.repaymentType = repaymentRequestDTO.getRepaymentType();
        this.repaymentTypeID = repaymentRequestDTO.getRepaymentTypeID();
        this.paymentDate = repaymentRequestDTO.getPaymentDate();
        this.amountPaid = repaymentRequestDTO.getAmountPaid();
    }
}