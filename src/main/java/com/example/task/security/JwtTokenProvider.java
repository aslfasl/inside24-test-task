package com.example.task.security;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String SECRET = "secretsecretsecretsecretsecretsecret";
    int tokenDurationInMillis = 9999 * 1000 * 60;

    public String createToken(String username){
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("username", username);
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenDurationInMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, SECRET.getBytes())
                .compact();

    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(SECRET.getBytes()).parseClaimsJws(token);
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Jwt token is expired or invalid");
        }
    }
}
