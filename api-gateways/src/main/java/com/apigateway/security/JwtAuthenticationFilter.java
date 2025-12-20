package com.apigateway.security;

import org.springframework.http.HttpCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        // No Authorization header → continue (public endpoints)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            return chain.filter(exchange);
        }

        var claims = jwtUtil.getClaims(token);

        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        var auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        return chain.filter(exchange)
                .contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(auth)
                );
    }
}
