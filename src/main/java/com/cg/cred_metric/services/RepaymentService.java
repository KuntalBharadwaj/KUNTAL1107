package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.repayment.RepaymentRequestDTO;
import com.cg.cred_metric.dtos.repayment.RepaymentResponseDTO;
import com.cg.cred_metric.exceptions.InvalidInputException;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.models.Loan;
import com.cg.cred_metric.models.Repayment;
import com.cg.cred_metric.models.User;
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

import java.time.LocalDate;
import java.util.Objects;

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

    @Override
    @Transactional
    public ResponseEntity<RepaymentResponseDTO> createRepayment(String email, RepaymentRequestDTO repaymentRequestDTO) {
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Loan loan = loanRepository.findById(repaymentRequestDTO.getRepaymentTypeID())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with ID: " + repaymentRequestDTO.getRepaymentTypeID()));

        // Check if loan is already completed, no further repayment should be allowed
        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new InvalidInputException("This loan has already been completed, no further repayments are allowed.");
        }

        Double amountPaid = repaymentRequestDTO.getAmountPaid();
        Double emi = loan.getEmiAmount();

        if (!Objects.equals(amountPaid, emi)) {
            throw new InvalidInputException("Amount paid must be equal to EMI amount: " + emi);
        }

        LocalDate dueDate = loan.getEmiDueDate();
        LocalDate paidDate = repaymentRequestDTO.getPaymentDate();

        Repayment.RepaymentStatus status = paidDate.isAfter(dueDate)
                ? Repayment.RepaymentStatus.MISSED
                : Repayment.RepaymentStatus.ONTIME;

        // Create and save repayment
        Repayment repayment = new Repayment();
        repayment.setUser(user);
        repayment.setRepaymentType(repaymentRequestDTO.getRepaymentType());
        repayment.setRepaymentTypeID(repaymentRequestDTO.getRepaymentTypeID());
        repayment.setPaymentDate(paidDate);
        repayment.setRepaymentStatus(status);
        repayment.setAmountPaid(amountPaid);
        repaymentRepository.save(repayment);


        // Update loan EMI due date
        loan.setEmiDueDate(loan.getEmiDueDate().plusMonths(1));

        // Check if all EMIs are paid and mark loan as closed
        if (loan.getEmiDueDate().isAfter(loan.getEndDate())) {
            loan.setStatus(Loan.LoanStatus.CLOSED);  // Mark loan as closed
        }

        loanRepository.save(loan);

        // Send Email
        String repaymentMessage = "Repayment Created Successfully!\n\n" +
                "Hi " + user.getName() + ",\n\n" +
                "Your repayment has been successfully added in Cred Metric.\n\n" +
                "Repayment Details:\n\n" +
                "Repayment Type: " + repaymentRequestDTO.getRepaymentType() + "\n" +
                "Repayment Type ID: " + repaymentRequestDTO.getRepaymentTypeID() + "\n" +
                "Repayment Date: " + repaymentRequestDTO.getPaymentDate() + "\n" +
                "Repayment Status: " + status + "\n" +
                "Repayment Amount Paid: " + amountPaid + "\n\n" +
                "Thank you for trusting Cred Metric with your financial journey. 🔒\n\n" +
                "Warm regards,\n" +
                "**Cred Metric Team**";
        mailService.sendMail(user.getEmail(), "Your repayment has been created successfully", repaymentMessage);

        return new ResponseEntity<>(new RepaymentResponseDTO(repayment), HttpStatus.CREATED);
    }
}
