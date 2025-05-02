package com.cg.cred_metric.controllers;

import com.cg.cred_metric.dtos.repayment.RepaymentRequestDTO;
import com.cg.cred_metric.dtos.repayment.RepaymentResponseDTO;
import com.cg.cred_metric.services.RepaymentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/repayments")
@Slf4j
public class RepaymentController {
    @Autowired
    private RepaymentService repaymentService;

    @PostMapping
    public ResponseEntity<RepaymentResponseDTO> createRepayment(Authentication authentication, @Valid @RequestBody RepaymentRequestDTO repaymentRequestDTO) {
        return repaymentService.createRepayment(authentication.getName(), repaymentRequestDTO);
    }
}
