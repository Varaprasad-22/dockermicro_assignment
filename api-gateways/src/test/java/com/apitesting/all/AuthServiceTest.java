package com.apitesting.all;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import com.apigateway.dto.MessageResponse;
import com.apigateway.dto.SignUpRequest;
import com.apigateway.model.ERole;
import com.apigateway.model.Role;
import com.apigateway.model.User;
import com.apigateway.repository.RoleRepository;
import com.apigateway.repository.UserRepository;
import com.apigateway.security.JwtUtil;
import com.apigateway.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtils;

    @Mock
    org.springframework.security.authentication.ReactiveAuthenticationManager authenticationManager;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setup() {
    	//no need creating at each case itself
    }

    @Test
    void register_success() {
        SignUpRequest req = new SignUpRequest();
        req.setUsername("newuser");
        req.setEmail("newuser@example.com");
        req.setPassword("plain");
        req.setRole(Set.of()); // empty -> default ROLE_USER

        when(userRepository.existsByUsername(("newuser"))).thenReturn(false);
        when(userRepository.existsByEmail(("newuser@example.com"))).thenReturn(false);

        Role userRole = new Role();
        userRole.setId( 2L);
        userRole.setName(ERole.ROLE_USER);

        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plain")).thenReturn("encoded");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });

        StepVerifier.create(authService.register(req))
                .assertNext(msg -> {
                    assert msg instanceof MessageResponse;
                    assert ((MessageResponse) msg).getMessage().toLowerCase().contains("registered");
                })
                .verifyComplete();
    }

    @Test
    void register_usernameExists_returnsErrorMessage() {
        SignUpRequest req = new SignUpRequest();
        req.setUsername("exists");
        req.setEmail("x@example.com");
        req.setPassword("p");

        when(userRepository.existsByUsername("exists")).thenReturn(true);

        StepVerifier.create(authService.register(req))
                .assertNext(msg -> {
                    assert msg.getMessage().contains("Username is already taken");
                })
                .verifyComplete();
    }
}
