package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.CreditCard.CreditCardDTO;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.models.CreditCard;
import com.cg.cred_metric.models.Loan;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.CreditCardRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Double billDueAmount = creditCardDTO.getCreditLimit()
                .subtract(creditCardDTO.getCurrentBalance())
                .doubleValue();

        CreditCard creditCard = new CreditCard();
        creditCard.setUser(user);
        creditCard.setCreditLimit(creditCardDTO.getCreditLimit());
        creditCard.setCurrentBalance(creditCardDTO.getCurrentBalance());
        creditCard.setIssueDate(creditCardDTO.getIssueDate());
        creditCard.setExpiryDate(creditCardDTO.getExpiryDate());
        creditCard.setCardBillAmount(billDueAmount);
        creditCard.setBillDueDate(creditCardDTO.getBillDueDate());

        CreditCard savedCard = creditCardRepository.save(creditCard);

        String creditCardMessage = "Credit Card Added Successfully!"
                + "\n\nHi, " + user.getName() + "!"
                + "\n\nYour credit card has been successfully added to your profile in Cred Metric. \nHere are the details of your card:"
                + "\n\nCredit Limit: ₹" + creditCardDTO.getCreditLimit()
                + "\nCard ID:" +  savedCard.getCardId()
                + "\nCurrent Balance: ₹" + creditCardDTO.getCurrentBalance()
                + "\nIssue Date:" + creditCardDTO.getIssueDate()
                + "\nExpiry Date: " + creditCardDTO.getExpiryDate()
                + "\nCard Bill Amount: " + billDueAmount
                + "\nCard Bill Due Date: " + creditCardDTO.getBillDueDate()
                + "\n\nWe’re excited to have you on board. Please review your card details carefully. If you notice anything unusual or didn’t authorize this card, contact our support team immediately."
                + "\n\nWarm regards,"
                + "\nCred Metric Team";

        mailService.sendMail(user.getEmail(), "💳 Your Credit Card Has Been Added!", creditCardMessage);

        // Save the card
        return creditCard;
    }

    //Update an existing credit card
    @Transactional
    public CreditCard updateCreditCard(Long cardId, String email, CreditCardDTO creditCardDTO) {

        CreditCard card = creditCardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        String cardHolderEmail = card.getUser().getEmail();

        if(!cardHolderEmail.equals(email)) {
            throw new ResourceNotFoundException("You are not allowed to update this credit card");
        }

        Double billDueAmount = creditCardDTO.getCreditLimit()
                .subtract(creditCardDTO.getCurrentBalance())
                .doubleValue();

        CreditCard creditCard = card;
        creditCard.setCreditLimit(creditCardDTO.getCreditLimit());
        creditCard.setCurrentBalance(creditCardDTO.getCurrentBalance());
        creditCard.setIssueDate(creditCardDTO.getIssueDate());
        creditCard.setExpiryDate(creditCardDTO.getExpiryDate());
        creditCard.setCardBillAmount(billDueAmount);
        creditCard.setBillDueDate(creditCardDTO.getBillDueDate());

        // Reset reminder sent for next due date
        creditCard.setReminderSent(false);

        return creditCardRepository.save(creditCard);
    }

    // Fetch credit card details for a specific user
    public List<CreditCard> getCreditCardsForUser(String email) {
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return creditCardRepository.findByUser(user);
    }

    // Fetch a specific credit card by card ID
    public CreditCard getCreditCardById(Long cardId) {
        Optional<CreditCard> creditCard = creditCardRepository.findById(cardId);
        if (creditCard.isEmpty()) {
            throw new ResourceNotFoundException("Credit card not found");
        }
        return creditCard.get();
    }

    // Delete Card By User
    @Transactional
    public void deleteCreditCardsByUser(String email) {
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        creditCardRepository.deleteByUser(user);
    }

    // To delete credit card for user by using credit card ID
    @Transactional
    public boolean deleteCreditCardForUser(Long cardId, String userEmail) {
        Optional<CreditCard> optionalCreditCard = creditCardRepository.findById(cardId);

        if (optionalCreditCard.isEmpty()) return false;

        CreditCard creditCard = optionalCreditCard.get();
        if (!creditCard.getUser().getEmail().equals(userEmail)) {
            return false; // Not the owner
        }

        creditCardRepository.delete(creditCard);
        return true;
    }
}