package com.cg.cred_metric.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Data
@NoArgsConstructor
public class ResetPasswordDTO {

    @NotBlank
    @Email(message = "Enter the valid email")
    private String email;

    @NotBlank(message = "otp can't be empty")
    private String otp;

    @NotBlank(message = "password can't be empty")
    private String newPassword;

    @NotBlank(message = "password can't be empty")
    private String confirmPassword;
}
