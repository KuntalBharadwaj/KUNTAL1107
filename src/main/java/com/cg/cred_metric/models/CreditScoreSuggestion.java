package com.cg.cred_metric.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "suggestion_month")
    private YearMonth suggestionMonth;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
