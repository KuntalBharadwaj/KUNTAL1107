package com.cg.cred_metric.utils;


import com.cg.cred_metric.exceptions.ExpiredTokenException;
import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.UserRespository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JWTUtils {


    @Autowired
    private UserRespository userRepository;

    private static final String SECRET_KEY = "AB1245EFGHI6789KLMNINHAUWONGOANAGIRBN";

    // Create Token
    public String createJWTToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)),SignatureAlgorithm.HS256)
                //.signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    // Email Extract kri
    public String extractEmail(String token) {
        try{
            System.out.printf("Extracting email from token: %s",token);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    //.setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("\nEmail is: "+claims.getSubject());

            return claims.getSubject();
        }
        catch (Exception e){
            // Token expire
            return e.getMessage();
        }
    }

    // Validate Token
    public boolean validateJWTToken(String token, String userEmail) {
        String email = extractEmail(token);

        if (!email.equals(userEmail)) {
            throw new ExpiredJwtException(null, null, "Invalid token or mismatched email.");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getToken() == null) {
            throw new ExpiredTokenException("Token has expired. Please login again to get a new token.");
        }

        if (!user.getToken().equals(token)) {
            throw new ExpiredTokenException("Token has expired. Please login again to get a new token.");
        }

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (claims.getExpiration().before(new Date())) {
            throw new ExpiredTokenException("Token has expired. Please login again to get a new token.");
        }

        return true;
    }
}
