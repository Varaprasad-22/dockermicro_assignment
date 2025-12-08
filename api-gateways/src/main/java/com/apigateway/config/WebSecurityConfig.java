package com.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.apigateway.security.JwtAuthenticationConverter;
import com.apigateway.security.ReactiveAuthenticationEntryPoint;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

	private final String role_admin="ROLE_ADMIN";
	private final String role_user="ROLE_USER";
    private final ReactiveAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final ReactiveUserDetailsService userDetailsService;

    public WebSecurityConfig(ReactiveAuthenticationEntryPoint unauthorizedHandler,
                            JwtAuthenticationConverter jwtAuthenticationConverter,
                            ReactiveUserDetailsService userDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(PasswordEncoder passwordEncoder) {
        // Create password-based authentication manager for signin
        UserDetailsRepositoryReactiveAuthenticationManager passwordAuthManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        passwordAuthManager.setPasswordEncoder(passwordEncoder);
        
        // Return a smart authentication manager that handles both cases:
        // 1. JWT tokens (has authorities, no credentials) - already validated, return as-is
        // 2. Username/password (has credentials, no authorities yet) - validate using password manager
        return authentication -> {
            // If authentication has authorities but no credentials, it's a JWT token
            // (JWT tokens are validated in JwtAuthenticationConverter and have authorities set)
            if (authentication.getCredentials() == null && 
                authentication.getAuthorities() != null && 
                !authentication.getAuthorities().isEmpty()) {
                // JWT token case - already validated, return as authenticated
                return Mono.just(authentication);
            }
            // Otherwise, it's username/password authentication - validate using password manager
            return passwordAuthManager.authenticate(authentication);
        };
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                          ReactiveAuthenticationManager authenticationManager) {
        // Create JWT authentication filter
        AuthenticationWebFilter jwtAuthenticationFilter = new AuthenticationWebFilter(authenticationManager);
        jwtAuthenticationFilter.setServerAuthenticationConverter(jwtAuthenticationConverter);
        jwtAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(
        		 "/api/flights/airline/inventory",          
                 "/api/flights/inventory/updateSeats",      

                 // Booking / ticket endpoints (BookingController)
                 "/api/flight/booking/**",                 
                 "/api/flight/ticket/**",                   
                 "/api/flight/booking/history/**",           
                 "/api/flight/booking/cancel/**"     
        ));

        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - no authentication required
                		 .pathMatchers("/api/auth/**").permitAll()

                         // Flight search and get details are public
                         .pathMatchers("/api/flights/search").permitAll()
                         .pathMatchers("/api/flights/{flightId}").permitAll()
                         .pathMatchers("/api/flights/*").permitAll()

                         // Protected endpoints - require authentication with specific roles
                         // Flight inventory operations -> ADMIN only
                         .pathMatchers("/api/flights/airline/inventory").hasAnyAuthority(role_admin)
                         .pathMatchers("/api/flights/inventory/updateSeats").hasAnyAuthority(role_admin)

                         // Booking & ticket operations -> USER or ADMIN
                         .pathMatchers("/api/flight/booking/**").hasAnyAuthority("role_user", role_admin)
                         .pathMatchers("/api/flight/ticket/**").hasAnyAuthority("role_user", role_admin)
                         .pathMatchers("/api/flight/booking/history/**").hasAnyAuthority("role_user", role_admin)
                         .pathMatchers("/api/flight/booking/cancel/**").hasAnyAuthority("role_user", role_admin)
                		
                		.anyExchange().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());

        return http.build();
    }
}

