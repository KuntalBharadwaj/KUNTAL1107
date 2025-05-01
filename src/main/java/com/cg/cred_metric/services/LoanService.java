package com.cg.cred_metric.services;


import com.cg.cred_metric.dtos.Loan.LoanRequestDTO;
import com.cg.cred_metric.dtos.Loan.LoanResponseDTO;
import com.cg.cred_metric.models.Loan;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.LoanRepository;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    @Autowired
    private final LoanRepository loanRepository;
    private final UserRespository userRepository;
    
    @Autowired
    private final MailService mailService;

    @Transactional
    public Loan createLoan(String email, LoanRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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


        String loanMessage = "✅ **Loan Created Successfully!** ✅\n\n" +
                "Hi **" + user.getName() + "**,\n\n" +
                "Your loan has been successfully created in Cred Metric. Here are the details of your loan:\n\n" +
                "**Loan Type:** " + dto.getLoanType() + "\n" +
                "**Principal Amount:** ₹" + dto.getPrincipalAmount() + "\n" +
                "**Interest Rate:** " + dto.getInterestRate() + "%\n" +
                "**Start Date:** " + dto.getStartDate() + "\n" +
                "**End Date:** " + dto.getEndDate() + "\n" +
                "**EMI Amount:** ₹" + dto.getEmiAmount() + "\n" +
                "**Next EMI Due Date:** " + dto.getEmiDueDate() + "\n\n" +
                "Thank you for trusting Cred Metric with your financial journey. If you have any questions or didn’t authorize this loan, please contact our support team immediately. 🔒\n\n" +
                "Warm regards,\n" +
                "**Cred Metric Team** 🚀";

        mailService.sendMail(user.getEmail(), "📄 Your Loan Has Been Created Successfully!", loanMessage);
        
        return loanRepository.save(loan);
    }

    // Get all loans
    public List<LoanResponseDTO> getAllLoansByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Loan> loans = loanRepository.findByUser(user);

        // Loan Entity → LoanResponseDTO me convert karo
        return loans.stream().map(loan -> {
            LoanResponseDTO dto = new LoanResponseDTO();
            dto.setLoanType(loan.getLoanType().name());
            dto.setPrincipalAmount(loan.getPrincipalAmount());
            dto.setInterestRate(loan.getInterestRate());
            dto.setStartDate(loan.getStartDate());
            dto.setEndDate(loan.getEndDate());
            dto.setEmiAmount(loan.getEmiAmount());
            dto.setEmiDueDate(loan.getEmiDueDate());
            dto.setStatus(loan.getStatus().name());
            return dto;
        }).toList();
    }

    // Update Loan
    public Loan updateLoan(Long loanId, String email, LoanRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!loan.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized to update this loan");
        }

        // Update allowed fields
        loan.setLoanType(Loan.LoanType.valueOf(dto.getLoanType().toUpperCase()));
        loan.setPrincipalAmount(dto.getPrincipalAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setStartDate(dto.getStartDate());
        loan.setEndDate(dto.getEndDate());
        loan.setEmiAmount(dto.getEmiAmount());
        loan.setEmiDueDate(dto.getEmiDueDate());

        String updateMessage = "📢 **Loan Updated!** 📢\n\n" +
                "Hi **" + user.getName() + "**,\n\n" +
                "Your loan details have been updated. Here are the latest loan details:\n\n" +
                "**Loan Type:** " + dto.getLoanType() + "\n" +
                "**Principal Amount:** ₹" + dto.getPrincipalAmount() + "\n" +
                "**Interest Rate:** " + dto.getInterestRate() + "%\n" +
                "**Start Date:** " + dto.getStartDate() + "\n" +
                "**End Date:** " + dto.getEndDate() + "\n" +
                "**EMI Amount:** ₹" + dto.getEmiAmount() + "\n" +
                "**Next EMI Due Date:** " + dto.getEmiDueDate() + "\n\n" +
                "If you didn’t request this update, please reach out to our support team immediately.\n\n" +
                "Warm regards,\n" +
                "**Cred Metric Team** 🔐";

        mailService.sendMail(user.getEmail(), "📝 Loan Updated Successfully", updateMessage);

        return loanRepository.save(loan);
    }

}
