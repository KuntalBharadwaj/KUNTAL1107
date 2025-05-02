package com.cg.cred_metric.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private double score;

    private LocalDateTime calculatedAt;

    @PrePersist
    public void onCreate() {
        this.calculatedAt = LocalDateTime.now();
    }
}
