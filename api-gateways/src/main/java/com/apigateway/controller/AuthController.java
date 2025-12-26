package com.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;

import com.apigateway.dto.ChangePasswordRequest;
import com.apigateway.dto.ForgotPasswordRequest;
import com.apigateway.dto.JwtResponse;
import com.apigateway.dto.LoginRequest;
import com.apigateway.dto.MessageResponse;
import com.apigateway.dto.ResetPasswordRequest;
import com.apigateway.dto.SignUpRequest;
import com.apigateway.security.JwtUtil;
import com.apigateway.service.AuthService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

	private final AuthService authService;
	public AuthController(AuthService authService, JwtUtil jwtUtil) {
		this.authService = authService;
		this.jwtUtil = jwtUtil;
	}

	@PostMapping("/signin")
	public Mono<ResponseEntity<Object>> login(
	        @Valid @RequestBody LoginRequest request) {

	    return authService.authenticate(request)
	            .map(jwt -> ResponseEntity.ok((Object) jwt))
	            .onErrorResume(
	                    org.springframework.security.authentication.CredentialsExpiredException.class,
	                    ex -> Mono.just(
	                            ResponseEntity
	                                    .status(HttpStatus.FORBIDDEN)
	                                    .body(new MessageResponse("PASSWORD_EXPIRED"))
	                    )
	            );
	}



	@PostMapping("/signout")
	public Mono<ResponseEntity<MessageResponse>> logout() {
		return Mono.just(ResponseEntity.ok(new MessageResponse("Logged out successfully")));
	}

	@PostMapping("/signup")
	public Mono<ResponseEntity<Object>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {

		return authService.register(signUpRequest).map(messageResponse -> {
			if (messageResponse.getMessage().contains("Error")) {
				return ResponseEntity.badRequest().body(messageResponse);
			}
			return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
		});
	}

	@PostMapping("/change-password")
	public Mono<ResponseEntity<MessageResponse>> changePassword(
	        @RequestHeader("Authorization") String authHeader,
	        @Valid @RequestBody ChangePasswordRequest request) {

	    // extract token
	    String token = authHeader.substring(7); // remove "Bearer "

	    // get username from token
	    String username = jwtUtil.getClaims(token).getSubject();

	    return authService.changePassword(username, request)
	            .map(ResponseEntity::ok);
	}

	//for expired Password
	@PostMapping("/changeOnExpire")
	public Mono<ResponseEntity<MessageResponse>> changeOnExpire(
			@RequestBody ChangePasswordRequest request){
		return authService.changePassword(request.getName(), request)
				.map(ResponseEntity::ok);
	}
	
	//reset and forgot passwords
	@PostMapping("/forgot-password")
	public Mono<ResponseEntity<MessageResponse>> forgotPassword(
	        @RequestBody ForgotPasswordRequest request) {

	    return authService.forgotPassword(request.getEmail())
	            .map(ResponseEntity::ok);
	}

	@PostMapping("/reset-password")
	public Mono<ResponseEntity<MessageResponse>> resetPassword(
	        @RequestBody ResetPasswordRequest request) {

	    return authService.resetPassword(
	            request.getToken(),
	            request.getNewPassword()
	    ).map(ResponseEntity::ok);
	}


}
