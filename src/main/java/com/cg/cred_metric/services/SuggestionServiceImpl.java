package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.SuggestionResponse;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
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

/**
 * Service implementation for handling credit score suggestions.
 */
@Service
public class SuggestionServiceImpl implements ISuggestionService {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserRespository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Retrieves credit score suggestions for a given user and month.
     *
     * @param email The email of the user.
     * @param month The month for which suggestions are requested.
     * @return SuggestionResponse containing the suggestions list or empty if not available.
     */
    @Override
    public SuggestionResponse getSuggestionsForUserAndMonth(String email, YearMonth month) {
        // Fetch the user by email, throw exception if not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long userId = user.getUserId();

        // Fetch the suggestion for the user and month
        CreditScoreSuggestion suggestion = suggestionRepository.findByUserAndSuggestionMonth(user, month);

        // If no suggestion is found, return an empty list in the response
        if (suggestion == null) {
            return new SuggestionResponse(userId, month, Collections.emptyList());
        }

        List<String> suggestionList;
        try {
            // Parse JSON string of suggestions into a List<String>
            suggestionList = objectMapper.readValue(suggestion.getSuggestions(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // Handle any parsing errors
            throw new RuntimeException("Error parsing suggestion JSON", e);
        }

        // Return the suggestions response with the parsed list
        return new SuggestionResponse(userId, month, suggestionList);
    }
}