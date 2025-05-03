package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface IUserService {
    ResponseEntity<AuthResponseDTO> registerUser(RegisterDTO registerDTO);
    ResponseEntity<AuthResponseDTO> loginUser(LoginDTO loginDTO);
    ResponseEntity<ChangePasswordResponseDTO> changePassword(String email, ChangePasswordRequestDTO request);

    ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO);
    ResponseEntity<?> forgetPassword(String email);
    void deleteUser(String email);
}
