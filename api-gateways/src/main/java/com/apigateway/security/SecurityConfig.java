package com.apigateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity http, JwtAuthenticationFilter jwtFilter,
			ReactiveAuthenticationEntryPoint entryPoint) {

		return http.csrf(ServerHttpSecurity.CsrfSpec::disable).formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

				.exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))

				.authorizeExchange(ex -> ex
            			.pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

//.pathMatchers("/api/auth/change-password").authenticated()

					    .pathMatchers("/api/auth/**").permitAll()

					    .pathMatchers("/api/flights/airline/inventory/**").hasRole("ADMIN")

					    .pathMatchers("/api/flights/search").permitAll()
					    .pathMatchers("/api/flights/*").permitAll()

					    .pathMatchers("/api/flight/booking").hasAnyRole("USER", "ADMIN")

					    .pathMatchers("/api/flight/booking/**").hasAnyRole("USER", "ADMIN")
					    .pathMatchers("/api/flight/ticket/**").hasAnyRole("USER", "ADMIN")

					    .anyExchange().authenticated()
					)

				.addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)

//				.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)

				.build();
	}
}
