package com.cg.cred_metric.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue()
    private Long cardId;

    @ManyToOne(fetch = FetchType.LAZY) // Fetch Meaning: Data is loaded only when needed (on-demand).
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private BigDecimal creditLimit;

    private BigDecimal currentBalance;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
