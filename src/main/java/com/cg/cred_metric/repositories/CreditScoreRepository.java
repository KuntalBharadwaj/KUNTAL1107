package com.cg.cred_metric.repositories;

import com.cg.cred_metric.models.CreditScore;
import com.cg.cred_metric.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditScoreRepository extends JpaRepository<CreditScore, Long> {
    Optional<CreditScore> findByUser(User user);
}
