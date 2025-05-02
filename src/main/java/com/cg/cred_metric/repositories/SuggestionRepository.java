package com.cg.cred_metric.repositories;

import com.cg.cred_metric.models.CreditScoreSuggestion;
import com.cg.cred_metric.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;



@Repository
public interface SuggestionRepository extends JpaRepository<CreditScoreSuggestion, Long> {
    CreditScoreSuggestion findByUserAndSuggestionMonth(User user, YearMonth suggestionMonth);
}
