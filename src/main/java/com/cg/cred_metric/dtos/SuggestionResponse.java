package com.cg.cred_metric.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;


@NoArgsConstructor
@Data
public class SuggestionResponse {
    private Long userId;
    private YearMonth month;
    private List<String> suggestions;

}
