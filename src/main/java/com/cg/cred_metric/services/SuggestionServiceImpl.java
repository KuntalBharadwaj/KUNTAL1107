package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.SuggestionResponse;
import com.cg.cred_metric.models.CreditScoreSuggestion;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.SuggestionRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

@Service
public class SuggestionServiceImpl implements ISuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserRespository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public SuggestionResponse getSuggestionsForUserAndMonth(String email, YearMonth month) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Long userId = user.getUserId();

        CreditScoreSuggestion suggestion = suggestionRepository.findByUserAndSuggestionMonth(user, month);

        if (suggestion == null) {
            return new SuggestionResponse(userId, month, Collections.emptyList());
        }

        List<String> suggestionList;
        try {
            suggestionList = objectMapper.readValue(suggestion.getSuggestions(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing suggestion JSON", e);
        }

        return new SuggestionResponse(userId, month, suggestionList);
    }
}
