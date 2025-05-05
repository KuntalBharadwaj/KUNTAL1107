package com.cg.cred_metric.dtos.creditscore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Outer wrapper response
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditScoreResponseDTO {
    private int code;
    private CreditScoreDataDTO data;
    private String message;
}
