package com.cg.cred_metric.controllers;


import com.cg.cred_metric.dtos.*;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.services.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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


    // Get me
    @GetMapping("/loggedIn")
    public User getCurrentUser() {
            return userService.getMe();
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

    // Change password
    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponseDTO> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequestDTO requestDTO) {
        String email = authentication.getName(); // this gets the username/email from JWT token
        return userService.changePassword(email, requestDTO);
    }

    // forget pass
    @PostMapping("/forget-password")
    public ResponseEntity<?> forgotPassowrd(@RequestBody String email){
        return userService.forgetPassword(email);
    }

    // reset pass
    @PutMapping("reset-password")
    public ResponseEntity<?> resetPassowrd(@RequestBody ResetPasswordDTO resetPasswordDTO){
        return userService.resetPassword(resetPasswordDTO);
    }
    // Delete User by email
    @DeleteMapping("/")
    public ResponseEntity<String> deleteUser(Authentication authentication) {
        try {
            userService.deleteUser(authentication.getName());
            return ResponseEntity.ok("User deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }



}
