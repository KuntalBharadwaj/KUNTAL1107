package com.cg.cred_metric.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loans")
@NoArgsConstructor
@Data
public class Loan {
    @Id
    @GeneratedValue
    private Long loanId;

    @ManyToOne(fetch = FetchType.LAZY) // Fetch Meaning: Data is loaded only when needed (on-demand).
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType; // SECURED or UNSECURED

    private Double principalAmount;
    private Double interestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double emiAmount;
    private LocalDate emiDueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status; // ACTIVE, CLOSED
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum LoanStatus {
        ACTIVE,
        CLOSED
    }

    public enum LoanType {
        SECURED,
        UNSECURED
    }
}
