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

    String otp="";


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

        // Validate the date of birth (check if the user is at least 18)
        LocalDate dob = registerDTO.getDateOfBirth();

        // Validate the date format and check age
            if (dob == null) {
                throw new InvalidDateFormatException("Date of birth is required.");
            }
            // Check if the user is at least 18 years old
            if (Period.between(dob, LocalDate.now()).getYears() < 18) {
                throw new InvalidDateFormatException("User must be at least 18 years old.");
            }


        // Create new user
        User user = new User();
        user.setName(registerDTO.getName());
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        user.setPassword(encodedPassword);
        user.setEmail(registerDTO.getEmail());
        user.setDateOfBirth(dob);

        // Save user
        userRespository.save(user);

        // log.info("User created" + user);
        String subject = "Welcome to Cred Metric!";
        String body = "🎉 **Welcome to Cred Metric!** 🎉\n\n" +
                "This is your one-stop app where you can easily view and track your **credit score**. " +
                "Whether you're looking to improve your financial health or simply keep tabs on your credit status, we've got you covered!\n\n" +
                "Start exploring and take control of your credit journey today! 🚀";

        mailService.sendMail(registerDTO.getEmail(), subject, body);

        // Return success response
        return new ResponseEntity<>(
                new AuthResponseDTO("User registered successfully, with user id: ", user.getUserId()),
                HttpStatus.CREATED
        );
    }


    @Override
    public ResponseEntity<AuthResponseDTO> loginUser(LoginDTO loginDTO) {

        User user = userRespository.findByEmail(loginDTO.getEmail()).orElseThrow(() -> new ResourceNotFoundException( "User not found with email " + loginDTO.getEmail()));

        if (user == null) {
            return new ResponseEntity<>(new AuthResponseDTO("Invalid Email", ""), HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return new ResponseEntity<>(new AuthResponseDTO("Invalid Password", ""), HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtils.createJWTToken(user.getEmail());

        user.setToken(token);
        userRespository.save(user);

        String loginMessage = "🔑 **Your Login was Successful!** 🔑\n\n" +
                "Hi **" + user.getName() + "**, \n\n" +
                "You're all set and logged in to Cred Metric! 🎉\n\n" +
                "We’re excited to have you back. If this wasn't you, please reach out immediately to secure your account. We take your security seriously. 🔒\n\n" +
                "Feel free to explore your dashboard and make the most out of your account.\n\n" +
                "Happy exploring! 🚀";

        mailService.sendMail(user.getEmail(), "🔐 Your Cred Metric Login Was Successful! 🚀", loginMessage);

        AuthResponseDTO responseDTO = new AuthResponseDTO("Login successful", token);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChangePasswordResponseDTO> changePassword(String email, ChangePasswordRequestDTO request) {
        User user = userRespository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException( "User not found with email " + email));

        // 1. Old password check
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return new ResponseEntity<>(new ChangePasswordResponseDTO(400, "Old password is incorrect"), HttpStatus.BAD_REQUEST);
        }

        // 2. New password same as old
        if (request.getOldPassword().equals(request.getNewPassword())) {
            return new ResponseEntity<>(new ChangePasswordResponseDTO(400, "New password should not be same as old password"), HttpStatus.BAD_REQUEST);
        }

        // 3. Confirm password mismatch
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return new ResponseEntity<>(new ChangePasswordResponseDTO(400, "New password and confirm password do not match"), HttpStatus.BAD_REQUEST);
        }

        // All validations passed, update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRespository.save(user);

        return new ResponseEntity<>(new ChangePasswordResponseDTO(200, "Password changed successfully"), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> forgetPassword(String email) {

        log.info("Forget password email: " + email);

        // Check user exist hai ya nhi
        User user = userRespository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException( "User not found with email " + email));

        log.info(String.valueOf(user));


        //User Exist krta hai otp generate krke email pr bhejo
        otp = otpGenerator.generateOTP();
        String token = jwtUtils.createJWTToken(email);

        mailService.sendMail(email, "OTP generated", "OTP to reset password is: " + otp);

        return new ResponseEntity<>(new AuthResponseDTO("OTP generated  ", otp), HttpStatus.OK);

    }

    @Override
    public ResponseEntity<?> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String email = resetPasswordDTO.getEmail();

        // Check user exist hai ya nhi
        User user = userRespository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException( "User not found with email " + email));


        //Verify OTP
        if(!otp.equals(resetPasswordDTO.getOtp())){
            throw new ResourceNotFoundException("OTP doesn't match");
        }

        // New Pass ko encode krna hai
        if(!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())){
            throw new ResourceNotFoundException("Confirm password doesn't match");
        }

        String encodedNewPassword = passwordEncoder.encode(resetPasswordDTO.getNewPassword());

        user.setPassword(encodedNewPassword);

        userRespository.save(user);

        otp="";

        mailService.sendMail(email, "Password Request", "Password changed successfully.");
        return new ResponseEntity<>(new AuthResponseDTO("Reset password successful", user.getUserId()), HttpStatus.OK);

    }


    // Delete User by Email
    @Transactional
    public void deleteUser(String email) {
        User user = userRespository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        userRespository.delete(user);
    }

}