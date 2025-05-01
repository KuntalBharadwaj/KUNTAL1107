package com.cg.cred_metric.repositories;

import com.cg.cred_metric.models.CreditScoreSuggestion;
import jdk.jshell.SourceCodeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionRepository extends JpaRepository<CreditScoreSuggestion, Long> {
}
