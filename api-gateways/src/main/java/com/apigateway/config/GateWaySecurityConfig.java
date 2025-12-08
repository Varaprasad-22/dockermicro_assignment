package com.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class GateWaySecurityConfig {

	 @Bean
	    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	        http
	            .csrf(csrf -> csrf.disable())
	            .authorizeExchange(ex -> ex
	                .pathMatchers("/api/auth/**").permitAll()
	                .anyExchange().authenticated()
	            )
	            .oauth2ResourceServer(oauth2 -> oauth2.jwt());
	        return http.build();
	    }
}
