package com.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;

import com.apigateway.dto.JwtResponse;
import com.apigateway.dto.LoginRequest;
import com.apigateway.dto.MessageResponse;
import com.apigateway.dto.SignUpRequest;
import com.apigateway.service.AuthService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<JwtResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        return authService.authenticate(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/signout")
    public Mono<ResponseEntity<MessageResponse>> logout() {
        return Mono.just(
                ResponseEntity.ok(
                        new MessageResponse("Logged out successfully")
                )
        );
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<Object>> registerUser(
            @Valid @RequestBody SignUpRequest signUpRequest) {

        return authService.register(signUpRequest)
                .map(messageResponse -> {
                    if (messageResponse.getMessage().contains("Error")) {
                        return ResponseEntity.badRequest().body(messageResponse);
                    }
                    return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
                });
    }
}
