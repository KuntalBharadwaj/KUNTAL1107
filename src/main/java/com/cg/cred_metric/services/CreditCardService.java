package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.CreditCard.CreditCardDTO;
import com.cg.cred_metric.models.CreditCard;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.CreditCardRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private MailService mailService;
    // Add a new credit card
    @Transactional
    public CreditCard addCreditCard(String email, CreditCardDTO creditCardDTO) {
        // Check if user exists

        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CreditCard creditCard = new CreditCard();
        creditCard.setUser(user);
        creditCard.setCreditLimit(creditCardDTO.getCreditLimit());
        creditCard.setCurrentBalance(creditCardDTO.getCurrentBalance());
        creditCard.setIssueDate(creditCardDTO.getIssueDate());
        creditCard.setExpiryDate(creditCardDTO.getExpiryDate());

        CreditCard savedCard = creditCardRepository.save(creditCard);

        String creditCardMessage = "✅ **Credit Card Added Successfully!** ✅\n\n" +
                "Hi " + user.getName() + "**,\n\n" +
                "Your credit card has been successfully added to your profile in Cred Metric. Here are the details of your card:\n\n" +
                "Credit Limit: ₹" + creditCardDTO.getCreditLimit() + "\n" +
                "Card ID:" +  savedCard.getCardId() + "\n" +
                "Current Balance: ₹" + creditCardDTO.getCurrentBalance() + "\n" +
                "Issue Date:" + creditCardDTO.getIssueDate() + "\n" +
                "Expiry Date: " + creditCardDTO.getExpiryDate() + "\n\n" +
                "We’re excited to have you on board. Please review your card details carefully. If you notice anything unusual or didn’t authorize this card, contact our support team immediately. 🔒\n\n" +
                "Warm regards,\n" +
                "Cred Metric Team 🚀";

        mailService.sendMail(user.getEmail(), "💳 Your Credit Card Has Been Added!", creditCardMessage);


        // Save the card
        return creditCard;


    }

     //Update an existing credit card
    @Transactional
    public CreditCard updateCreditCard(Long cardId, CreditCardDTO creditCardDTO) {
        Optional<CreditCard> optionalCreditCard = creditCardRepository.findById(cardId);
        if (optionalCreditCard.isEmpty()) {
            throw new IllegalArgumentException("Credit card not found");
        }

        CreditCard creditCard = optionalCreditCard.get();
        creditCard.setCreditLimit(creditCardDTO.getCreditLimit());
        creditCard.setCurrentBalance(creditCardDTO.getCurrentBalance());
        creditCard.setIssueDate(creditCardDTO.getIssueDate());
        creditCard.setExpiryDate(creditCardDTO.getExpiryDate());

        return creditCardRepository.save(creditCard);
    }

    // Fetch credit card details for a specific user

    public List<CreditCard> getCreditCardsForUser(String email) {
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return creditCardRepository.findByUser(user);
    }

    // Fetch a specific credit card by card ID
    public CreditCard getCreditCardById(Long cardId) {
        Optional<CreditCard> creditCard = creditCardRepository.findById(cardId);
        if (creditCard.isEmpty()) {
            throw new IllegalArgumentException("Credit card not found");
        }
        return creditCard.get();
    }
}

