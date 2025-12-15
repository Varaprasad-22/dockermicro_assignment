package com.apitesting.all;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import com.apigateway.model.ERole;
import com.apigateway.model.Role;
import com.apigateway.model.User;
import com.apigateway.repository.UserRepository;
import com.apigateway.service.UserDetailsServiceImpl; // the service under test

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    @Test
    void findByUsername_whenUserExists_returnsUserDetails() {
        User u = new User();
        u.setId(1L);
        u.setUsername("vara");
        u.setEmail("vara@example.com");
        u.setPassword("passpass");
        Role role = new Role(ERole.ROLE_USER);
        u.setRoles(Set.of(role));

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(u));

        StepVerifier.create(userDetailsService.findByUsername("vara"))
                .assertNext(details -> {
                    assert details != null;
                    assert details.getUsername().equals("vara");
                    assert details.getPassword().equals("passpass");
                })
                .verifyComplete();
    }

    @Test
    void findByUsername_whenNotFound_emitsError() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        StepVerifier.create(userDetailsService.findByUsername("varas"))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        || (throwable.getMessage() != null && throwable.getMessage().contains("User Not Found")))
                .verify();
    }

}
