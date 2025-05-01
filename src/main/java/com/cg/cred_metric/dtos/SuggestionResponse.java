package com.cg.cred_metric.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuggestionResponse {
    private Long userId;
    private YearMonth month;
    private List<String> suggestions;

}
