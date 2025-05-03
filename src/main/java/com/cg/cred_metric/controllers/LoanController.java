package com.cg.cred_metric.controllers;


import com.cg.cred_metric.dtos.Loan.LoanRequestDTO;
import com.cg.cred_metric.dtos.Loan.LoanResponseDTO;
import com.cg.cred_metric.services.LoanService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/loans")
@RestController
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(Authentication authentication,
                                             @Valid @RequestBody LoanRequestDTO loanRequestDTO) {

        return loanService.createLoan(authentication.getName(), loanRequestDTO);
    }

    @GetMapping
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans(Authentication authentication) {
        String email = authentication.getName(); // Logged-in user ka email
        List<LoanResponseDTO> loans = loanService.getAllLoansByEmail(email);
        return ResponseEntity.ok(loans);
    }

    @PutMapping("/{loanId}")
    public ResponseEntity<?> updateLoan(@PathVariable Long loanId,
                                             Authentication authentication,
                                             @Valid @RequestBody LoanRequestDTO loanRequestDTO) {
        loanService.updateLoan(loanId, authentication.getName(), loanRequestDTO);
        return ResponseEntity.ok(
                Map.of(
                        "status", 200,
                        "data", Map.of("message", "Loan Updated successfully")
                )
        );
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteLoansByUser(Authentication authentication) {
        loanService.deleteLoansByUser(authentication.getName());

        return ResponseEntity.ok(
                Map.of(
                        "status", 200,
                        "data", Map.of("message", "Loan Deleted successfully")
                )
        );
    }

}
