package com.cg.cred_metric.services;



import java.time.YearMonth;

import com.cg.cred_metric.dtos.SuggestionResponse;

public interface ISuggestionService {
    SuggestionResponse getSuggestionsForUserAndMonth(String email, YearMonth month);
}
