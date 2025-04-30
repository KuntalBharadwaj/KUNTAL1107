package com.cg.cred_metric.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/helloworld")
    public String helloworld() {
        return "Hello World";
    }

    // Health Check Api
    @GetMapping("/health-check")
    public String healthCheck() {
        return "Everthing working fine in auth";
    }
}
