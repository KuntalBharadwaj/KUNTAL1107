package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.repayment.RepaymentRequestDTO;
import com.cg.cred_metric.dtos.repayment.RepaymentResponseDTO;
import org.springframework.http.ResponseEntity;

public interface IRepaymentService {
    ResponseEntity<RepaymentResponseDTO> createRepayment(String email, RepaymentRequestDTO repaymentRequestDTO);
}

