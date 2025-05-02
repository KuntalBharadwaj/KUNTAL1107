package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.*;
import org.springframework.http.ResponseEntity;

public interface IUserService {
    ResponseEntity<AuthResponseDTO> registerUser(RegisterDTO registerDTO);
    ResponseEntity<AuthResponseDTO> loginUser(LoginDTO loginDTO);
    ResponseEntity<ChangePasswordResponseDTO> changePassword(String email, ChangePasswordRequestDTO request);

    void deleteUser(String email);
}
