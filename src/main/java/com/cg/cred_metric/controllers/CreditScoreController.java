package com.cg.cred_metric.controllers;


import com.cg.cred_metric.dtos.creditscore.CreditScoreDataDTO;
import com.cg.cred_metric.dtos.creditscore.CreditScoreResponseDTO;
import com.cg.cred_metric.services.CreditScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/creditscore")
public class CreditScoreController {

    // as it is a final type we can't do field injection here
    private final CreditScoreService creditScoreService;

    @Autowired
    public CreditScoreController(CreditScoreService creditScoreService) {
        this.creditScoreService = creditScoreService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CreditScoreResponseDTO> getCreditScore(Authentication authentication, @PathVariable Long userId) {
        int score = creditScoreService.calculateScoreForUser(userId); // This is the correct method
        CreditScoreDataDTO creditScoreDataDTO = new CreditScoreDataDTO(LocalDate.now(),score);
        CreditScoreResponseDTO creditscoreresponse = new CreditScoreResponseDTO(200,
                creditScoreDataDTO,
                "Credit score fetched successfully");
        return new ResponseEntity<>(creditscoreresponse, HttpStatus.OK);
    }
}
