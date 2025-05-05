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

    // Repository for interacting with Loan database table
    @Autowired
    private final LoanRepository loanRepository;

    // Repository for interacting with User database table
    private final UserRespository userRepository;

    // Service to send emails
    @Autowired
    private final MailService mailService;

    /**
     * Creates a loan for a user and sends confirmation email.
     *
     * @param email User's email
     * @param dto Loan request data
     * @return ResponseEntity with LoanResponseDTO
     */
    @Transactional
    public ResponseEntity<LoanResponseDTO> createLoan(String email, LoanRequestDTO dto) {
        // Retrieve user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create new loan entity from DTO
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

        // Build confirmation email content
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

        // Send email to user
        mailService.sendMail(user.getEmail(), "📄 Your Loan Has Been Created Successfully!", loanMessage);

        // Save loan to database
        loanRepository.save(loan);

        // Return loan response
        LoanResponseDTO loanResponseDTO = new LoanResponseDTO(loan);
        return new ResponseEntity<>(loanResponseDTO, HttpStatus.CREATED);
    }

    /**
     * Fetch all loans for a user by email.
     *
     * @param email User's email
     * @return List of LoanResponseDTOs
     */
    public List<LoanResponseDTO> getAllLoansByEmail(String email) {
        // Validate user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Retrieve loans by user
        List<Loan> loans = loanRepository.findByUser(user);

        // Convert loan entities to response DTOs
        return loans.stream()
                .map(LoanResponseDTO::new)
                .toList();
    }

    /**
     * Update loan details for a given loan ID.
     *
     * @param loanId ID of the loan to update
     * @param email  Email of the user requesting the update
     * @param dto    Updated loan details
     * @return Updated Loan entity
     */
    public Loan updateLoan(Long loanId, String email, LoanRequestDTO dto) {
        // Validate user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Fetch loan by ID
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // Check if the user owns this loan
        if (!loan.getUser().getUserId().equals(user.getUserId())) {
            throw new ResourceNotFoundException("Unauthorized to update this loan");
        }

        // Update loan fields
        loan.setLoanType(Loan.LoanType.valueOf(dto.getLoanType().toUpperCase()));
        loan.setPrincipalAmount(dto.getPrincipalAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setStartDate(dto.getStartDate());
        loan.setEndDate(dto.getEndDate());
        loan.setEmiAmount(dto.getEmiAmount());
        loan.setEmiDueDate(dto.getEmiDueDate());

        // Send confirmation email
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

        // Reset reminder status
        loan.setReminderSent(false);

        return loanRepository.save(loan);
    }

    /**
     * Delete all loans for a specific user.
     *
     * @param email User's email
     */
    @Transactional
    public void deleteLoansByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        loanRepository.deleteByUser(user);
    }

    /**
     * Delete a specific loan by loan ID for a user.
     *
     * @param loanId    ID of the loan
     * @param userEmail Email of the user
     * @return true if deleted, false otherwise
     */
    @Transactional
    public boolean deleteLoanForUser(Long loanId, String userEmail) {
        Optional<Loan> optionalLoan = loanRepository.findById(loanId);

        // Loan does not exist
        if (optionalLoan.isEmpty()) return false;

        Loan loan = optionalLoan.get();

        // Verify ownership before deleting
        if (!loan.getUser().getEmail().equals(userEmail)) {
            return false;
        }

        loanRepository.delete(loan);
        return true;
    }
}