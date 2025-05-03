package com.cg.cred_metric.controllers;


import com.cg.cred_metric.dtos.AuthResponseDTO;
import com.cg.cred_metric.dtos.LoginDTO;
import com.cg.cred_metric.dtos.RegisterDTO;
import com.cg.cred_metric.services.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    // Health Check Api
    @GetMapping("/health-check")
    public String healthCheck() {
        return "Everthing working fine in auth";
    }

    // Register API
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO registerRequest){
        return userService.registerUser(registerRequest);
    }

    // Login API
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO){
        return userService.loginUser(loginDTO);
    }

}
