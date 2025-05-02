package com.cg.cred_metric.controllers;

import com.cg.cred_metric.dtos.CreditCard.CreditCardDTO;
import com.cg.cred_metric.dtos.CreditCard.CreditCardResponseDTO;
import com.cg.cred_metric.models.CreditCard;
import com.cg.cred_metric.services.CreditCardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;


import java.util.List;

@RestController
@RequestMapping("/credit-cards")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    // POST /creditcards — Add a new credit card
    @PostMapping
    public ResponseEntity<CreditCardResponseDTO> addCreditCard(Authentication authentication,
                                                               @Valid @RequestBody CreditCardDTO creditCardDTO) {
        CreditCard creditCard = creditCardService.addCreditCard(authentication.getName(), creditCardDTO);
        return new ResponseEntity<>(new CreditCardResponseDTO(creditCard), HttpStatus.CREATED);
    }

    // PUT /creditcards/{id} — Update an existing credit card
    @PutMapping("/{id}")
    public ResponseEntity<CreditCardResponseDTO> updateCreditCard(Authentication authentication,
                                                                  @PathVariable Long id,
                                                                  @Valid @RequestBody CreditCardDTO creditCardDTO) {
        CreditCard updatedCard = creditCardService.updateCreditCard(id, creditCardDTO);
        return ResponseEntity.ok(new CreditCardResponseDTO(updatedCard));
    }

    // GET /creditcards — Get all credit cards for a user
    @GetMapping("/")
    public ResponseEntity<List<CreditCard>> getCardsByUser(Authentication authentication) {
        List<CreditCard> cards = creditCardService.getCreditCardsForUser(authentication.getName());
        return ResponseEntity.ok(cards);
    }

    // GET /creditcards/{cardId} — Get a specific credit card by card ID
    @GetMapping("/{cardId}")
    public ResponseEntity<CreditCardResponseDTO> getCardById(Authentication authentication,
                                                             @PathVariable Long cardId) {
        CreditCard card = creditCardService.getCreditCardById(cardId);
        return ResponseEntity.ok(new CreditCardResponseDTO(card));
    }
}

