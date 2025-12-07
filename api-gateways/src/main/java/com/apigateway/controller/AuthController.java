package com.apigateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apigateway.util.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtUtil jwtUtil;
    private final String cookieName;
    private final long expireSeconds;
    public AuthController(JwtUtil jwtUtil,
            @Value("${jwt.cookieName}") String cookieName,
            @Value("${jwt.expireSeconds}") long expireSeconds) {
this.jwtUtil = jwtUtil;
this.cookieName = cookieName;
this.expireSeconds = expireSeconds;
}
    
    
    
}
