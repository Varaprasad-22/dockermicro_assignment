package com.apigateway.config;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;

@Configuration
public class JwtAuthConverterConfig {

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
    	Converter<Jwt, Collection<GrantedAuthority>> converter = (Jwt jwt) -> {
            Object claim = jwt.getClaims().get("roles");
            if (claim == null) return List.of();

            List<String> roles;
            if (claim instanceof List) {
                //noinspection unchecked
                roles = (List<String>) claim;
            } else {
                roles = List.of(claim.toString());
            }

            return roles.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(r -> r.startsWith("ROLE_") ? r : ("ROLE_" + r))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        };

        JwtAuthenticationConverter jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(converter);
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthConverter);
    }
}
