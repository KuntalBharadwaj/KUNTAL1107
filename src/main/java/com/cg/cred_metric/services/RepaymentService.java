package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.repayment.RepaymentRequestDTO;
import com.cg.cred_metric.dtos.repayment.RepaymentResponseDTO;
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

import java.util.Objects;

@Service
@Slf4j
public class RepaymentService implements IRepaymentService{
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

        User user = userRespository.findByEmail(email).orElse(null);
        Loan loan = loanRepository.findById(repaymentRequestDTO.getRepaymentTypeID()).orElseThrow(() -> new RuntimeException("Loan not found"));

        if(loan == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Have to throw exception
        }

        Double amountPaid = repaymentRequestDTO.getAmountPaid();
        Double emi = loan.getEmiAmount();

        if (!Objects.equals(amountPaid, emi)) {
            log.error("Amount paid not equal to EMI amount");
            return new ResponseEntity<>( HttpStatus.NOT_FOUND); // throw Exception
        }



        Repayment repayment = new Repayment(repaymentRequestDTO);
        repayment.setUser(user);
        repaymentRepository.save(repayment);

        RepaymentResponseDTO repaymentResponseDTO = new RepaymentResponseDTO(repayment);

        String repaymentMessage = "Repayment Created Successfully!\n\n" +
                "Hi " + user.getName() + ",\n\n" +
                "Your repayment has been successfully added in Cred Metric.\n\n" +
                "Repayment Details:\n\n" +
                "Repayment Type: " + repaymentRequestDTO.getRepaymentType() + "\n" +
                "Repayment Type ID: " + repaymentRequestDTO.getRepaymentTypeID() + "\n" +
                "Repayment Date: " + repaymentRequestDTO.getPaymentDate() + "%\n" +
                "Repayment Status: " + repaymentRequestDTO.getRepaymentStatus() + "\n" +
                "Repayment Amount Paid: " + repaymentRequestDTO.getAmountPaid() + "\n" +
                "Thank you for trusting Cred Metric with your financial journey. If you have any questions or didn’t authorize this loan, please contact our support team immediately. 🔒\n\n" +
                "Warm regards,\n" +
                "**Cred Metric Team**";
        mailService.sendMail(user.getEmail(), "Your repayment has been created successfully", repaymentMessage);
        return new ResponseEntity<>(repaymentResponseDTO, HttpStatus.CREATED);
    }
}
