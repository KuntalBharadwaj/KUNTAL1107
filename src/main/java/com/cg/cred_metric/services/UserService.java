package com.cg.cred_metric.services;

import com.cg.cred_metric.dtos.AuthResponseDTO;
import com.cg.cred_metric.dtos.LoginDTO;
import com.cg.cred_metric.dtos.RegisterDTO;
import com.cg.cred_metric.exceptions.InvalidDateFormatException;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.UserRespository;
import com.cg.cred_metric.utils.JWTUtils;
import com.cg.cred_metric.utils.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        User user = userRespository.findByEmail(loginDTO.getEmail()).orElse(null);

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


}