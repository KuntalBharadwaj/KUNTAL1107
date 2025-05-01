package com.cg.cred_metric.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Data
@NoArgsConstructor
public class CreditScoreSuggestion {
    @Id
    @GeneratedValue(strategy  = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private YearMonth yearMonth;

    @Column(columnDefinition = "json") // For MySQL
    private String suggestions; // JSON array as String

    private LocalDateTime createdAt = LocalDateTime.now();

}
