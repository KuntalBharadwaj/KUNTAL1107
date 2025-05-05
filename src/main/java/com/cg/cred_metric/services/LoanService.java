package com.cg.cred_metric.services;


import com.cg.cred_metric.dtos.Loan.LoanRequestDTO;
import com.cg.cred_metric.dtos.Loan.LoanResponseDTO;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.models.Loan;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.LoanRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoanService {

    @Autowired
    private final LoanRepository loanRepository;
    private final UserRespository userRepository;
    
    @Autowired
    private final MailService mailService;

    @Transactional
    public ResponseEntity<LoanResponseDTO> createLoan(String email, LoanRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setLoanType(Loan.LoanType.valueOf(dto.getLoanType().toUpperCase()));
        loan.setPrincipalAmount(dto.getPrincipalAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setStartDate(dto.getStartDate());
        loan.setEndDate(dto.getEndDate());
        loan.setEmiAmount(dto.getEmiAmount());
        loan.setEmiDueDate(dto.getEmiDueDate());
        loan.setStatus(Loan.LoanStatus.ACTIVE);

        String loanMessage = "Loan Created Successfully!"
                + "\n\nHi, " + user.getName() + "!"
                + "\n\nYour loan has been successfully created in Cred Metric. \nHere are the details of your loan:"
                + "\n\nLoan Type: " + dto.getLoanType()
                + "\nPrincipal Amount: ₹" + dto.getPrincipalAmount()
                + "\nInterest Rate: " + dto.getInterestRate() + "%"
                + "\nStart Date: " + dto.getStartDate()
                + "\nEnd Date: " + dto.getEndDate()
                + "\nEMI Amount: ₹" + dto.getEmiAmount()
                + "\nNext EMI Due Date: " + dto.getEmiDueDate()
                + "\n\nThank you for trusting Cred Metric with your financial journey. If you have any questions or didn’t authorize this loan, please contact our support team immediately."
                + "\n\nWarm regards,"
                + "\nCred Metric Team";

        mailService.sendMail(user.getEmail(), "📄 Your Loan Has Been Created Successfully!", loanMessage);
        
        loanRepository.save(loan);

        LoanResponseDTO loanResponseDTO = new LoanResponseDTO(loan);

        return new ResponseEntity<>(loanResponseDTO, HttpStatus.CREATED);
    }

    // Get all loans
    public List<LoanResponseDTO> getAllLoansByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Loan> loans = loanRepository.findByUser(user);

        // Loan Entity → LoanResponseDTO me convert karo
        return loans.stream().map(loan -> {
            LoanResponseDTO dto = new LoanResponseDTO(loan);
            return dto;
        }).toList();
    }

    // Update Loan
    public Loan updateLoan(Long loanId, String email, LoanRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getUserId().equals(user.getUserId())) {
            throw new ResourceNotFoundException("Unauthorized to update this loan");
        }

        // Update allowed fields
        loan.setLoanType(Loan.LoanType.valueOf(dto.getLoanType().toUpperCase()));
        loan.setPrincipalAmount(dto.getPrincipalAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setStartDate(dto.getStartDate());
        loan.setEndDate(dto.getEndDate());
        loan.setEmiAmount(dto.getEmiAmount());
        loan.setEmiDueDate(dto.getEmiDueDate());

        String updateMessage = "Loan Updated!"
                + "\n\nHi, " + user.getName() + "!"
                + "\n\nYour loan details have been updated. \nHere are the latest loan details:"
                + "\n\nLoan Type: " + dto.getLoanType()
                + "\nPrincipal Amount: ₹" + dto.getPrincipalAmount()
                + "\nInterest Rate: " + dto.getInterestRate() + "%"
                + "\nStart Date: " + dto.getStartDate()
                + "\nEnd Date: " + dto.getEndDate()
                + "\nEMI Amount: ₹" + dto.getEmiAmount()
                + "\nNext EMI Due Date: " + dto.getEmiDueDate()
                + "\n\nIf you didn’t request this update, please reach out to our support team immediately."
                + "\n\nWarm regards,"
                + "\nCred Metric Team";

        mailService.sendMail(user.getEmail(), "📝 Loan Updated Successfully", updateMessage);

        // Reset reminder sent for next due date
        loan.setReminderSent(false);

        return loanRepository.save(loan);
    }

    // Delete Loan By User
    @Transactional
    public void deleteLoansByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        loanRepository.deleteByUser(user);
    }

    // To delete loan for user by using loan ID
    @Transactional
    public boolean deleteLoanForUser(Long loanId, String userEmail) {
        Optional<Loan> optionalLoan = loanRepository.findById(loanId);

        if (optionalLoan.isEmpty()) return false;

        Loan loan = optionalLoan.get();
        if (!loan.getUser().getEmail().equals(userEmail)) {
            return false; // Not the owner
        }

        loanRepository.delete(loan);
        return true;
    }
}
