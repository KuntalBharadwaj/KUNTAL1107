package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.AuthResponseDTO;
import com.cg.cred_metric.dtos.LoginDTO;
import com.cg.cred_metric.dtos.RegisterDTO;
import org.springframework.http.ResponseEntity;

public interface IUserService {
    public ResponseEntity<AuthResponseDTO> registerUser(RegisterDTO registerDTO);
    public ResponseEntity<AuthResponseDTO> loginUser(LoginDTO loginDTO);
}
