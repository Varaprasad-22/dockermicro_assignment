package com.apigateway.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final Key key;
	private final long expireTime;
	 public JwtUtil(
	            @Value("${jwt.secret}") String secret,
	            @Value("${jwt.expireSeconds}") long expireSeconds) {

	        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	        this.expireTime = expireSeconds * 1000;
	    }

	    public String generateToken(String username) {
	        Date now = new Date();
	        Date expiry = new Date(now.getTime() + expireTime);

	        return Jwts.builder()
	                .setSubject(username)
	                .setIssuedAt(now)
	                .setExpiration(expiry)
	                .signWith(key, SignatureAlgorithm.HS256)
	                .compact();
	    }

	    public Claims validate(String token) throws JwtException {
	        return Jwts.parserBuilder().setSigningKey(key).build()
	                .parseClaimsJws(token).getBody();
	    }
 

}
