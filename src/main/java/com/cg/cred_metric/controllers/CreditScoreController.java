package com.cg.cred_metric.controllers;


import com.cg.cred_metric.dtos.creditscore.CreditScoreDataDTO;
import com.cg.cred_metric.dtos.creditscore.CreditScoreResponseDTO;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.exceptions.UnAuthorizedUserException;
import com.cg.cred_metric.models.CreditScore;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.CreditScoreRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.services.CreditScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/creditscore")
public class CreditScoreController {

    // as it is a final type we can't do field injection here
    private final CreditScoreService creditScoreService;

    @Autowired
    public CreditScoreRepository creditScoreRepository;
    @Autowired
    private UserRespository userRespository;

    @Autowired
    public CreditScoreController(CreditScoreService creditScoreService) {
        this.creditScoreService = creditScoreService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CreditScoreResponseDTO> getCreditScore(Authentication authentication, @PathVariable Long userId) {
        User user = userRespository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user not found of this id : " + userId));
        String authenticateuseremail = authentication.getName();
        if(!user.getEmail().equals(authenticateuseremail)){
            throw new UnAuthorizedUserException("Unauthorized user");
        }

        CreditScore creditScore = creditScoreRepository.findByUser(user).orElseThrow(()-> new ResourceNotFoundException("credit score not found for this email"));

        CreditScoreDataDTO data = new CreditScoreDataDTO();
        data.setCreditScore(creditScore.getScore());
        data.setCalculatedDate(LocalDate.from(creditScore.getCalculatedAt()));

        CreditScoreResponseDTO responseDTO = new CreditScoreResponseDTO(HttpStatus.OK.value(),data,"credit score fetched");
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/recalculate/{userId}")
    public ResponseEntity<CreditScoreResponseDTO> calculateCreditScore(Authentication authentication, @PathVariable Long userId) {

        User user = userRespository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("user not found of this id : " + userId));
        String authenticateuseremail = authentication.getName();
        if(!user.getEmail().equals(authenticateuseremail)){
            throw new UnAuthorizedUserException("Unauthorized user");
        }

        int score = creditScoreService.calculateScoreForUser(userId); // This is the correct method
        CreditScoreDataDTO creditScoreDataDTO = new CreditScoreDataDTO(LocalDate.now(),score);
        CreditScoreResponseDTO creditscoreresponse = new CreditScoreResponseDTO(200,
                creditScoreDataDTO,
                "Credit score fetched successfully");
        return new ResponseEntity<>(creditscoreresponse, HttpStatus.OK);
    }
}
