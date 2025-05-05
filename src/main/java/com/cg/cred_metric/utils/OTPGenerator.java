package com.cg.cred_metric.utils;

import org.springframework.stereotype.Component;

@Component
public class OTPGenerator {
    public String generateOTP() {
        int otp = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(otp);
    }
}
