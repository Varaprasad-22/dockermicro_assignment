package com.apigateway.controller;

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
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
	public AuthController(AuthService authService) {
		this.authService=authService;
	}
    @PostMapping("/signin")
    public Mono<ResponseEntity<Object>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.authenticate(loginRequest)
                .map(jwtResponse -> ResponseEntity.ok((Object)jwtResponse))
                .onErrorResume(e -> {
                    MessageResponse errorResponse = new MessageResponse("Error: Invalid username or password!");
                    String response=errorResponse+"\n"+e.getMessage();
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
                });
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<Object>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.register(signUpRequest)
                .map(messageResponse -> {
                    if (messageResponse.getMessage().contains("Error")) {
                        return ResponseEntity.badRequest().body(messageResponse);
                    }
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body((Object)messageResponse);
                });
    }

    @PostMapping("/signout")
    public Mono<ResponseEntity<Object>> logoutUser() {
        return Mono.just(ResponseEntity.ok((Object)new MessageResponse("You've been signed out!")));
    }
}

