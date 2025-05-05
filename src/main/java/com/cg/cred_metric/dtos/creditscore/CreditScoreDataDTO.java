package com.cg.cred_metric.dtos.creditscore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Inner data object
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditScoreDataDTO {
    private LocalDate calculatedDate;
    private int creditScore;
}
