package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.*;
import com.cg.cred_metric.exceptions.InvalidDateFormatException;
import com.cg.cred_metric.exceptions.ResourceNotFoundException;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.JWTUtils;
import com.cg.cred_metric.utils.MailService;
import com.cg.cred_metric.utils.OTPGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@Slf4j
public class UserService implements IUserService {

    @Autowired
    private UserRespository userRespository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private MailService mailService;

    @Autowired
    OTPGenerator otpGenerator;

    // Stores OTP temporarily for password reset
    String otp = "";

    /*
     * Retrieves the currently authenticated user based on the security context.
     */
    @Override
    public User getMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRespository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
    }

    /*
     * Registers a new user after performing validations on email and age.
     */
    @Override
    public ResponseEntity<AuthResponseDTO> registerUser(RegisterDTO registerDTO) {
        // Check if user with the given email already exists
        if (userRespository.findByEmail(registerDTO.getEmail()).isPresent()) {
            return new ResponseEntity<>(
                    new AuthResponseDTO("User with this email already exists"),
                    HttpStatus.CONFLICT
            );
        }

        log.info(registerDTO.toString());

        // Validate date of birth
        LocalDate dob = registerDTO.getDateOfBirth();
        if (dob == null) {
            throw new InvalidDateFormatException("Date of birth is required.");
        }
        if (Period.between(dob, LocalDate.now()).getYears() < 18) {
            throw new InvalidDateFormatException("User must be at least 18 years old.");
        }

        // Create and save new user
        User user = new User();
        user.setName(registerDTO.getName());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setDateOfBirth(dob);
        userRespository.save(user);

        // Send welcome email
        String subject = "Welcome to Cred Metric!";
        String body = "🎉 Welcome to Cred Metric! 🎉"
                + "\n\nThank you for registering with Cred Metric!"
                + "\n\nThis is your one-stop app where you can easily view and track your credit score."
                + "\nWhether you're looking to improve your financial health or simply keeping tabs on your credit status, we've got you covered!"
                + "\n\nStart exploring and take control of your credit journey today! 🚀"
                + "\n\nWarm Regards,"
                + "\nTeam Cred Metric";
        mailService.sendMail(registerDTO.getEmail(), subject, body);

        return new ResponseEntity<>(
                new AuthResponseDTO("User registered successfully, with user id: ", user.getUserId()),
                HttpStatus.CREATED
        );
    }

    /*
     * Authenticates user using email and password, returns JWT on success.
     */
    @Override
    public ResponseEntity<AuthResponseDTO> loginUser(LoginDTO loginDTO) {
        User user = userRespository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found Exception " + loginDTO.getEmail()));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return new ResponseEntity<>(new AuthResponseDTO("Invalid Password", ""), HttpStatus.UNAUTHORIZED);
        }

        // Generate JWT and save
        String token = jwtUtils.createJWTToken(user.getEmail());
        user.setToken(token);
        userRespository.save(user);

        // Send login confirmation email
        String loginMessage = "Hi, " + user.getName() + "!"
                + "\n\nYou're all set and logged in to Cred Metric! 🎉"
                + "\n\nWe’re excited to have you back."
                + "\n\nIf this wasn't you, please reach out immediately to secure your account."
                + "\n\nHappy exploring! 🚀"
                + "\n\nWarm Regards,"
                + "\nTeam Cred Metric";
        mailService.sendMail(user.getEmail(), "Your Cred Metric Login Was Successful!", loginMessage);

        return new ResponseEntity<>(new AuthResponseDTO("Login successful", token), HttpStatus.OK);
    }

    /*
     * Changes the password for a given user after validating old password and new inputs.
     */
    @Override
    public ResponseEntity<ChangePasswordResponseDTO> changePassword(String email, ChangePasswordRequestDTO request) {
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found Exception " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return new ResponseEntity<>(new ChangePasswordResponseDTO(400, "Old password is incorrect"), HttpStatus.BAD_REQUEST);
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            return new ResponseEntity<>(new ChangePasswordResponseDTO(400, "New password should not be same as old password"), HttpStatus.BAD_REQUEST);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return new ResponseEntity<>(new ChangePasswordResponseDTO(400, "New password and confirm password do not match"), HttpStatus.BAD_REQUEST);
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRespository.save(user);

        // Send confirmation email
        String changePasswordMessage = "Hi, " + user.getName() + "!"
                + "\n\nYour password has been changed successfully!"
                + "\n\nIf this wasn't you, please contact us immediately."
                + "\n\nWarm Regards,"
                + "\nTeam Cred Metric";
        mailService.sendMail(user.getEmail(), "Your password has been changed successfully!", changePasswordMessage);

        return new ResponseEntity<>(new ChangePasswordResponseDTO(200, "Password changed successfully"), HttpStatus.OK);
    }

    /*
     * Sends OTP to user's email for resetting password.
     */
    @Override
    public ResponseEntity<?> forgetPassword(String email) {
        log.info("Forget password email: " + email);

        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));

        // Generate and send OTP
        otp = otpGenerator.generateOTP();
        mailService.sendMail(email, "OTP generated", "OTP to reset password is: " + otp);

        return new ResponseEntity<>(new AuthResponseDTO("OTP generated", otp), HttpStatus.OK);
    }

    /*
     * Resets the password after verifying OTP and confirming new password.
     */
    @Override
    public ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String email = resetPasswordDTO.getEmail();

        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + email));

        if (!otp.equals(resetPasswordDTO.getOtp())) {
            throw new ResourceNotFoundException("OTP doesn't match");
        }

        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new ResourceNotFoundException("Confirm password doesn't match");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        userRespository.save(user);
        otp = "";

        mailService.sendMail(email, "Password Request", "Password changed successfully.");

        return new ResponseEntity<>(new AuthResponseDTO("Reset password successful", user.getUserId()), HttpStatus.OK);
    }

    /*
     * Deletes a user by their email.
     */
    @Transactional
    public void deleteUser(String email) {
        User user = userRespository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        userRespository.delete(user);
    }
}