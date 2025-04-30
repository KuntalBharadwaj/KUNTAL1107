package com.cg.cred_metric.utils;


import com.cg.cred_metric.models.User;
import com.cg.cred_metric.repositories.UserRespository;
import io.jsonwebtoken.Claims;
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
        final String email = extractEmail(token);
        boolean isTokenPresent = true;
        User user = userRepository.findByEmail(email).orElse(null);

        if(user != null && user.getToken()==null) {
            isTokenPresent = false;
        }

        final boolean valid = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());

    return (email.equals(userEmail) && !valid && isTokenPresent);
    }
}
