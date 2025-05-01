package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.SuggestionResponse;
import com.cg.cred_metric.repositories.SuggestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;


@Service
public class SuggestionServiceImpl implements ISuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Override
    public SuggestionResponse getSuggestionsForUserAndMonth(Long userId, YearMonth month) {
            return null;
    }
}
