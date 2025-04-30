package com.cg.cred_metric.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    private String message;
    private String token;
    private Long userId;

    public AuthResponseDTO(String message) {
        this.message = message;
    }

    public AuthResponseDTO(String message, Long userId){
        this.message = message;
        this.userId = userId;
    }

    public AuthResponseDTO(String message, String token){
        this.message = message;
        this.token = token;
    }
}

