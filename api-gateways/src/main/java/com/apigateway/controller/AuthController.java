package com.apigateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apigateway.dto.LoginRequest;
import com.apigateway.dto.MessageResponse;
import com.apigateway.dto.SignUpRequest;
import com.apigateway.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
 
	@Autowired
    private  AuthService authService;

    @PostMapping("/signin")
    public Mono<ResponseEntity<?>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticate(loginRequest)
                .<ResponseEntity<?>>map(jwtResponse -> ResponseEntity.ok(jwtResponse))
                .onErrorResume(e -> {
                    MessageResponse errorResponse = new MessageResponse("Error: Invalid username or password!");
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()));
                });
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<?>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.register(signUpRequest)
                .map(messageResponse -> {
                    if (messageResponse.getMessage().contains("Error")) {
                        return ResponseEntity.badRequest().body(messageResponse);
                    }
                    return ResponseEntity.ok(messageResponse);
                });
    }

    @PostMapping("/signout")
    public Mono<ResponseEntity<?>> logoutUser() {
        return Mono.just(ResponseEntity.ok(new MessageResponse("You've been signed out!")));
    }
}

