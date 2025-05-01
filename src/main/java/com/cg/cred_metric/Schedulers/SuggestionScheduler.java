package com.cg.cred_metric.Schedulers;


import com.cg.cred_metric.repositories.SuggestionRepository;
import com.cg.cred_metric.repositories.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SuggestionScheduler {

    @Autowired
    private SuggestionRepository suggestionRepository;

    @Autowired
    private UserRespository userRespository;

    @Scheduled(cron = "0 0 1 1 * ?") // Runs at 1 AM on 1st of every month
    public void generateMonthlySuggestions() {

    }
}
