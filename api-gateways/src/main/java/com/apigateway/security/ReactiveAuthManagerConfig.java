package com.apigateway.security;

import com.apigateway.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ReactiveAuthManagerConfig {

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(
            UserDetailsServiceImpl userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);

        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }
}
