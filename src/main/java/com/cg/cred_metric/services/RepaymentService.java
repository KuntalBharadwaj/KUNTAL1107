package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.repayment.RepaymentRequestDTO;
import com.cg.cred_metric.dtos.repayment.RepaymentResponseDTO;
import com.cg.cred_metric.exceptions.InvalidInputException;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.models.CreditCard;
import com.cg.cred_metric.models.Loan;
import com.cg.cred_metric.models.Repayment;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.CreditCardRepository;
import com.cg.cred_metric.repositories.LoanRepository;
import com.cg.cred_metric.repositories.RepaymentRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.MailService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.cg.cred_metric.models.Loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Service class to handle creation of repayments for loans or credit cards.
 */
@Service
@Slf4j
public class RepaymentService implements IRepaymentService {

    @Autowired
    private RepaymentRepository repaymentRepository;

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private MailService mailService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CreditCardRepository creditCardRepository;

    /**
     * Creates a repayment based on the repayment type (loan or credit card).
     *
     * @param email               Email of the user making the repayment.
     * @param repaymentRequestDTO DTO containing repayment details.
     * @return ResponseEntity containing repayment details and next due date.
     */
    @Override
    @Transactional
    public ResponseEntity<RepaymentResponseDTO> createRepayment(String email, RepaymentRequestDTO repaymentRequestDTO) {

        // Retrieve user by email
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Object repaymentSource = null;
        LocalDate dueDate = null;

        // Handle loan repayment
        if (repaymentRequestDTO.getRepaymentType() == Repayment.RepaymentType.LOAN) {
            Loan loan = loanRepository.findById(repaymentRequestDTO.getRepaymentTypeID())
                    .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + repaymentRequestDTO.getRepaymentTypeID()));

            repaymentSource = loan;

            if (loan.getStatus() == LoanStatus.CLOSED) {
                throw new InvalidInputException("This loan has already been completed, no further repayments are allowed.");
            }

            if (repaymentRequestDTO.getAmountPaid() == 0) {
                throw new InvalidInputException("Repayment amount must be greater than zero.");
            }

            if (!Objects.equals(repaymentRequestDTO.getAmountPaid(), loan.getEmiAmount())) {
                throw new InvalidInputException("Amount paid must be equal to EMI amount: " + loan.getEmiAmount());
            }

            // Get current due date and update it for the next cycle
            dueDate = loan.getEmiDueDate();
            loan.setEmiDueDate(loan.getEmiDueDate().plusMonths(1));

            // Close the loan if end date has passed
            if (loan.getEmiDueDate().isAfter(loan.getEndDate())) {
                loan.setStatus(Loan.LoanStatus.CLOSED);
            }

            loanRepository.save(loan);
        }

        // Handle credit card repayment
        else if (repaymentRequestDTO.getRepaymentType() == Repayment.RepaymentType.CREDIT_CARD) {
            CreditCard creditCard = creditCardRepository.findById(repaymentRequestDTO.getRepaymentTypeID())
                    .orElseThrow(() -> new RuntimeException("CreditCard not found with ID: " + repaymentRequestDTO.getRepaymentTypeID()));

            repaymentSource = creditCard;

            if (creditCard.getExpiryDate().isBefore(LocalDate.now())) {
                throw new InvalidInputException("The credit card has expired.");
            }

            if (repaymentRequestDTO.getAmountPaid() == 0) {
                throw new InvalidInputException("Repayment amount must be greater than zero.");
            }

            if (!Objects.equals(repaymentRequestDTO.getAmountPaid(), creditCard.getCardBillAmount())) {
                throw new InvalidInputException("Amount paid must be equal to Card Bill Amount: " + creditCard.getCardBillAmount());
            }

            // Update bill due date and balances
            dueDate = creditCard.getBillDueDate();
            creditCard.setBillDueDate(creditCard.getBillDueDate().plusMonths(1));
            creditCard.setCardBillAmount(repaymentRequestDTO.getAmountPaid() - creditCard.getCardBillAmount());
            creditCard.setCurrentBalance(creditCard.getCurrentBalance().add(BigDecimal.valueOf(repaymentRequestDTO.getAmountPaid())));

            creditCardRepository.save(creditCard);
        }

        // Handle invalid repayment type
        else {
            throw new InvalidInputException("Invalid repayment type: " + repaymentRequestDTO.getRepaymentTypeID());
        }

        // Determine repayment status
        LocalDate paidDate = repaymentRequestDTO.getPaymentDate();
        Repayment.RepaymentStatus status = paidDate.isAfter(dueDate)
                ? Repayment.RepaymentStatus.MISSED
                : Repayment.RepaymentStatus.ONTIME;

        // Create and save repayment record
        Repayment repayment = new Repayment();
        repayment.setUser(user);
        repayment.setRepaymentType(repaymentRequestDTO.getRepaymentType());
        repayment.setRepaymentTypeID(repaymentRequestDTO.getRepaymentTypeID());
        repayment.setPaymentDate(paidDate);
        repayment.setRepaymentStatus(status);
        repayment.setAmountPaid(repaymentRequestDTO.getAmountPaid());
        repaymentRepository.save(repayment);

        // Compose email message
        String repaymentMessage = "Repayment Created Successfully!\n\n" +
                "Hi " + user.getName() + ",\n\n" +
                "Your repayment has been successfully added in Cred Metric.\n\n" +
                "Repayment Details:\n\n" +
                "Repayment Type: " + repaymentRequestDTO.getRepaymentType() + "\n" +
                "Repayment Type ID: " + repaymentRequestDTO.getRepaymentTypeID() + "\n" +
                "Repayment Date: " + repaymentRequestDTO.getPaymentDate() + "\n" +
                "Repayment Status: " + status + "\n" +
                "Repayment Amount Paid: " + repaymentRequestDTO.getAmountPaid();

        // Append next due date info
        if (repaymentSource instanceof Loan) {
            repaymentMessage += "\nNext EMI Due Date: " + ((Loan) repaymentSource).getEmiDueDate();
        } else if (repaymentSource instanceof CreditCard) {
            repaymentMessage += "\nNext Bill Due Date: " + ((CreditCard) repaymentSource).getBillDueDate();
        }

        repaymentMessage += "\n\nThank you for trusting Cred Metric with your financial journey. 🔒\n\n" +
                "Warm regards,\n" +
                "Cred Metric Team";

        // Send repayment confirmation email
        mailService.sendMail(user.getEmail(), "Your repayment has been created successfully", repaymentMessage);

        // Return response with next due date info
        return new ResponseEntity<>(
                new RepaymentResponseDTO(
                        repayment,
                        repaymentSource instanceof Loan
                                ? ((Loan) repaymentSource).getEmiDueDate()
                                : ((CreditCard) repaymentSource).getBillDueDate()
                ),
                HttpStatus.CREATED
        );
    }
}