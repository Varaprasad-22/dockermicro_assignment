package com.apigateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class GateWaySecurityConfig {

    private final ReactiveJwtAuthenticationConverterAdapter jwtConverter;

    public GateWaySecurityConfig(@Qualifier("jwtAuthenticationConverter") ReactiveJwtAuthenticationConverterAdapter jwtConverter) {
        this.jwtConverter = jwtConverter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(ex -> ex
                // make absolutely sure flight search endpoints are open
                .pathMatchers(HttpMethod.POST, "/api/flights/**").permitAll()
                // allow auth endpoints
                .pathMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .pathMatchers("/api/auth/**").permitAll()

                // admin add flight (keep protection)
                .pathMatchers(HttpMethod.POST, "/api/flights/airline/inventory").hasRole("ADMIN")

                // booking endpoints — you described
                .pathMatchers(HttpMethod.POST, "/api/flight/booking").authenticated()
                .pathMatchers(HttpMethod.DELETE, "/api/flight/booking/cancel/**").authenticated()
                .pathMatchers(HttpMethod.GET, "/api/flight/booking/history/**").authenticated()
                .pathMatchers(HttpMethod.GET, "/api/flight/ticket/**").permitAll()

                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter))
            );

        return http.build();
    }
}
