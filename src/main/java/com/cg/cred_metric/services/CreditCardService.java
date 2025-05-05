package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.CreditCard.CreditCardDTO;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.models.CreditCard;
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

    // Repository to interact with the Credit Card table in the database
    @Autowired
    private CreditCardRepository creditCardRepository;

    // Repository to interact with the User table in the database
    @Autowired
    private UserRespository userRespository;

    // Service to send emails to users
    @Autowired
    private MailService mailService;

    /**
     * Add a new credit card for a user.
     *
     * @param email User's email
     * @param creditCardDTO Data transfer object containing credit card details
     * @return Newly added CreditCard entity
     */
    @Transactional
    public CreditCard addCreditCard(String email, CreditCardDTO creditCardDTO) {
        // Validate if the user exists
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Calculate the bill due amount (Credit Limit - Current Balance)
        Double billDueAmount = creditCardDTO.getCreditLimit()
                .subtract(creditCardDTO.getCurrentBalance())
                .doubleValue();

        // Create a new CreditCard entity
        CreditCard creditCard = new CreditCard();
        creditCard.setUser(user);
        creditCard.setCreditLimit(creditCardDTO.getCreditLimit());
        creditCard.setCurrentBalance(creditCardDTO.getCurrentBalance());
        creditCard.setIssueDate(creditCardDTO.getIssueDate());
        creditCard.setExpiryDate(creditCardDTO.getExpiryDate());
        creditCard.setCardBillAmount(billDueAmount);
        creditCard.setBillDueDate(creditCardDTO.getBillDueDate());

        // Save the credit card in the database
        CreditCard savedCard = creditCardRepository.save(creditCard);

        // Construct email message
        String creditCardMessage = "Credit Card Added Successfully!"
                + "\n\nHi, " + user.getName() + "!"
                + "\n\nYour credit card has been successfully added to your profile in Cred Metric. \nHere are the details of your card:"
                + "\n\nCredit Limit: ₹" + creditCardDTO.getCreditLimit()
                + "\nCard ID: " + savedCard.getCardId()
                + "\nCurrent Balance: ₹" + creditCardDTO.getCurrentBalance()
                + "\nIssue Date: " + creditCardDTO.getIssueDate()
                + "\nExpiry Date: " + creditCardDTO.getExpiryDate()
                + "\nCard Bill Amount: " + billDueAmount
                + "\nCard Bill Due Date: " + creditCardDTO.getBillDueDate()
                + "\n\nWe’re excited to have you on board. Please review your card details carefully. If you notice anything unusual or didn’t authorize this card, contact our support team immediately."
                + "\n\nWarm regards,"
                + "\nCred Metric Team";

        // Send email to user
        mailService.sendMail(user.getEmail(), "💳 Your Credit Card Has Been Added!", creditCardMessage);

        // Return the saved CreditCard entity
        return creditCard;
    }

    /**
     * Update an existing credit card for a user.
     *
     * @param cardId ID of the card to be updated
     * @param email User's email
     * @param creditCardDTO Data transfer object containing updated credit card details
     * @return Updated CreditCard entity
     */
    @Transactional
    public CreditCard updateCreditCard(Long cardId, String email, CreditCardDTO creditCardDTO) {

        // Fetch the credit card by card ID
        CreditCard card = creditCardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card not found"));

        // Ensure the user owns the card
        String cardHolderEmail = card.getUser().getEmail();
        if (!cardHolderEmail.equals(email)) {
            throw new ResourceNotFoundException("You are not allowed to update this credit card");
        }

        // Calculate the updated bill due amount
        Double billDueAmount = creditCardDTO.getCreditLimit()
                .subtract(creditCardDTO.getCurrentBalance())
                .doubleValue();

        // Update card fields
        CreditCard creditCard = card;
        creditCard.setCreditLimit(creditCardDTO.getCreditLimit());
        creditCard.setCurrentBalance(creditCardDTO.getCurrentBalance());
        creditCard.setIssueDate(creditCardDTO.getIssueDate());
        creditCard.setExpiryDate(creditCardDTO.getExpiryDate());
        creditCard.setCardBillAmount(billDueAmount);
        creditCard.setBillDueDate(creditCardDTO.getBillDueDate());

        // Reset reminder status for the next due date
        creditCard.setReminderSent(false);

        // Save the updated card
        return creditCardRepository.save(creditCard);
    }

    /**
     * Fetch all credit cards associated with a user.
     *
     * @param email User's email
     * @return List of CreditCard entities for the user
     */
    public List<CreditCard> getCreditCardsForUser(String email) {
        // Validate if the user exists
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch all credit cards for the user
        return creditCardRepository.findByUser(user);
    }

    /**
     * Fetch a specific credit card by its card ID.
     *
     * @param cardId ID of the credit card
     * @return The CreditCard entity
     */
    public CreditCard getCreditCardById(Long cardId) {
        // Fetch the credit card by ID
        Optional<CreditCard> creditCard = creditCardRepository.findById(cardId);
        if (creditCard.isEmpty()) {
            throw new ResourceNotFoundException("Credit card not found");
        }
        return creditCard.get();
    }

    /**
     * Delete all credit cards associated with a specific user.
     *
     * @param email User's email
     */
    @Transactional
    public void deleteCreditCardsByUser(String email) {
        // Validate if the user exists
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Delete all credit cards for the user
        creditCardRepository.deleteByUser(user);
    }

    /**
     * Delete a specific credit card for a user by its card ID.
     *
     * @param cardId ID of the credit card to delete
     * @param userEmail Email of the user requesting the deletion
     * @return true if the card was deleted, false otherwise
     */
    @Transactional
    public boolean deleteCreditCardForUser(Long cardId, String userEmail) {
        Optional<CreditCard> optionalCreditCard = creditCardRepository.findById(cardId);

        // Check if the card exists
        if (optionalCreditCard.isEmpty()) return false;

        CreditCard creditCard = optionalCreditCard.get();

        // Verify if the user owns the card
        if (!creditCard.getUser().getEmail().equals(userEmail)) {
            return false; // Not the owner
        }

        // Delete the credit card
        creditCardRepository.delete(creditCard);
        return true;
    }
}