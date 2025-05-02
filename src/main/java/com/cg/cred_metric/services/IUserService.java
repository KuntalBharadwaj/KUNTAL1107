package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.*;
import org.springframework.http.ResponseEntity;

public interface IUserService {
    public ResponseEntity<AuthResponseDTO> registerUser(RegisterDTO registerDTO);
    public ResponseEntity<AuthResponseDTO> loginUser(LoginDTO loginDTO);
    public ResponseEntity<ChangePasswordResponseDTO> changePassword(String email, ChangePasswordRequestDTO request);
}
