package com.apigateway.service;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.apigateway.dto.JwtResponse;
import com.apigateway.dto.LoginRequest;
import com.apigateway.dto.MessageResponse;
import com.apigateway.dto.SignUpRequest;
import com.apigateway.model.ERole;
import com.apigateway.model.Role;
import com.apigateway.model.User;
import com.apigateway.repository.RoleRepository;
import com.apigateway.repository.UserRepository;
import com.apigateway.security.JwtUtil;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtils;
	private final ReactiveAuthenticationManager authenticationManager;

	public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
			JwtUtil jwtUtils, ReactiveAuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtils = jwtUtils;
		this.authenticationManager = authenticationManager;
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
}
