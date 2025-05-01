package com.cg.cred_metric.controllers;

import com.cg.cred_metric.dtos.SuggestionResponse;
import com.cg.cred_metric.services.ISuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.YearMonth;


@RestController
@RequestMapping("/suggestions")
public class SuggestionController {

    @Autowired
    private ISuggestionService suggestionService;

    @GetMapping("/{yearMonth}")
    public SuggestionResponse getSuggestionsForUserAndMonth(Authentication authentication, @PathVariable String yearMonth // format: YYYY-MM
    ) {

        YearMonth month = YearMonth.parse(yearMonth);
        return suggestionService.getSuggestionsForUserAndMonth(authentication.getName(), month);
    }
}
