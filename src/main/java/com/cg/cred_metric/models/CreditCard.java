package com.cg.cred_metric.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_cards")
@NoArgsConstructor
@Data
public class CreditCard {
    @Id
    @GeneratedValue
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(precision = 10, scale = 2)
    private BigDecimal creditLimit;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal currentBalance;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}
