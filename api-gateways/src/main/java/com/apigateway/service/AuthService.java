package com.apigateway.service;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.apigateway.dto.ChangePasswordRequest;
import com.apigateway.dto.JwtResponse;
import com.apigateway.dto.LoginRequest;
import com.apigateway.dto.MessageResponse;
import com.apigateway.dto.SignUpRequest;
import com.apigateway.model.ERole;
import com.apigateway.model.PasswordResetToken;
import com.apigateway.model.Role;
import com.apigateway.model.User;
import com.apigateway.repository.PasswordResetTokenRepository;
import com.apigateway.repository.RoleRepository;
import com.apigateway.repository.UserRepository;
import com.apigateway.security.JwtUtil;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final EmailService emailService;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtils;
	private final ReactiveAuthenticationManager authenticationManager;
	private final PasswordResetTokenRepository passwordResetRepository;
private final int password_expires_in=10;
	public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
			JwtUtil jwtUtils, ReactiveAuthenticationManager authenticationManager, EmailService emailService,PasswordResetTokenRepository passwordResetToken) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtils = jwtUtils;
		this.authenticationManager = authenticationManager;
		this.emailService = emailService;
		this.passwordResetRepository=passwordResetToken;
	}

	public Mono<MessageResponse> register(SignUpRequest signUpRequest) {
		return Mono.fromCallable(() -> {
			if (userRepository.existsByUsername(signUpRequest.getUsername())) {
				return new MessageResponse("Error: Username is already taken!");
			}

			if (userRepository.existsByEmail(signUpRequest.getEmail())) {
				return new MessageResponse("Error: Email is already in use!");
			}

			// Create new user's account
			User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
					passwordEncoder.encode(signUpRequest.getPassword()));
			user.setPasswordLastChangedAt(LocalDateTime.now());
			user.setPasswordExpired(false);
			Set<String> strRoles = signUpRequest.getRole();
			Set<Role> roles = new HashSet<>();

			if (strRoles == null || strRoles.isEmpty()) {
				Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(
						() -> new RuntimeException("Error: Role is not found need to be either user or admin."));
				roles.add(userRole);
			} else {
				strRoles.forEach(role -> {
					if (role.toLowerCase().equalsIgnoreCase("admin")) {

						Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
								.orElseThrow(() -> new RuntimeException("Error: admin is not found."));
						roles.add(adminRole);
					} else {
						Role userRole = roleRepository.findByName(ERole.ROLE_USER)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
						roles.add(userRole);
					}
				});
			}

			user.setRoles(roles);
			userRepository.save(user);

			return new MessageResponse("User registered successfully!");
		}).subscribeOn(Schedulers.boundedElastic());
	}

	public Mono<JwtResponse> authenticate(LoginRequest request) {

	    var authToken = new UsernamePasswordAuthenticationToken(
	            request.getUsername(),
	            request.getPassword()
	    );

	    return authenticationManager.authenticate(authToken)
	            .map(auth -> {
                UserDetailsImpl userDetails =
                        (UserDetailsImpl) auth.getPrincipal();

                System.out.println("email points = " + userDetails.getEmail());
                User user = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                LocalDateTime expireDate=user.getPasswordLastChangedAt().plusDays(password_expires_in);
                LocalDateTime currentDate = LocalDateTime.now().toLocalDate().atStartOfDay();	
                System.out.println("expire date"+expireDate);
                System.out.println("current Date"+currentDate);
                System.out.println("last Changed time"+user.getPasswordLastChangedAt());
                if(expireDate.isBefore(currentDate)) {
                	user.setPasswordExpired(true);
                	userRepository.save(user);
                	throw new RuntimeException("password_Expired");
                }
                
                user.setPasswordExpired(false);
                userRepository.save(user);
                String role = auth.getAuthorities().stream()
	                           .map(GrantedAuthority::getAuthority)
	                           .filter(r -> r.startsWith("ROLE_"))
	                           .map(r -> r.replace("ROLE_", ""))
	                           .findFirst()
	                           .orElse("USER");

	                String jwt = jwtUtils.generateToken(
	                        request.getUsername(),
	                        role
	                );

	                return new JwtResponse(
	                        jwt,
	                        request.getUsername(),
	                        role,
							 userDetails.getEmail() 
	                );
	            });
	}
	public Mono<MessageResponse> changePassword(
	        String username,
	        ChangePasswordRequest request) {

	    return Mono.fromCallable(() -> {

	        User user = userRepository.findByUsername(username)
	                .orElseThrow(() ->
	                        new RuntimeException("Error: User not found"));
	        if(request.getOldPassword().equals(request.getNewPassword())) {
	        	throw new RuntimeException("Old password should not be same as new Password");
	        }

	        if (!passwordEncoder.matches(
	                request.getOldPassword(),
	                user.getPassword())) {
	            throw new RuntimeException("Invalid old password");
	        }
	        user.setPassword(
	                passwordEncoder.encode(request.getNewPassword())
	        );
	        user.setPasswordLastChangedAt(LocalDateTime.now());
	        user.setPasswordExpired(false);
	        userRepository.save(user);

	        return new MessageResponse("Password updated successfully");

	    }).subscribeOn(Schedulers.boundedElastic());
	}

	public Mono<MessageResponse> forgotPassword(String email) {
	    return Mono.fromCallable(() -> {
	        User user = userRepository.findByEmail(email)
	                .orElseThrow(() -> new RuntimeException("User not found"));

	        String token = UUID.randomUUID().toString();

	        PasswordResetToken resetToken = new PasswordResetToken();
	        resetToken.setUser(user);
	        resetToken.setToken(token);
	        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

	        passwordResetRepository.deleteByUser(user);
	        passwordResetRepository.save(resetToken);

	        emailService.sendResetEmail(user.getEmail(), token);

	        return new MessageResponse("Reset link sent to email");
	    });
	}
	public Mono<MessageResponse> resetPassword(String token, String newPassword) {
	    return Mono.fromCallable(() -> {
	        PasswordResetToken resetToken = passwordResetRepository.findByToken(token)
	                .orElseThrow(() -> new RuntimeException("Invalid token"));

	        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
	            throw new RuntimeException("Token expired");
	        }

	        User user = resetToken.getUser();
	        user.setPassword(passwordEncoder.encode(newPassword));
	        user.setPasswordLastChangedAt(LocalDateTime.now());

	        userRepository.save(user);
	        passwordResetRepository.delete(resetToken);

	        return new MessageResponse("Password updated successfully");
	    });
	}


}
