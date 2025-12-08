package com.auth.util;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long validityMs;
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
      }

    public String generateToken(Authentication auth) {
        String username = auth.getName();
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);

        return Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key(),SignatureAlgorithm.HS256)
            .compact();
    }
    public long getValidityMs() { return validityMs; }
}
