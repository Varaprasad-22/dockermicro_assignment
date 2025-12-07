package com.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtGateway implements GlobalFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // You can set allowed open paths
    private final List<String> openPaths = List.of("/api/auth/", "/actuator", "/api/public/");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        for (String open : openPaths) {
            if (path.startsWith(open)) {
                return chain.filter(exchange);
            }
        }

        List<String> authHeaders = exchange.getRequest().getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION);
        if (authHeaders.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String header = authHeaders.get(0);
        if (!header.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = header.substring(7);

        return Mono.fromCallable(() -> parseClaims(token))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(claims -> {
                if (claims == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Optionally forward user info to downstream services via headers
                String username = claims.getSubject();
                Object rolesObj = claims.get("roles");
                String roles = (rolesObj != null) ? rolesObj.toString() : "";

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-Username", username)
                        .header("X-Auth-Roles", roles)
                        .build();

                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                return chain.filter(mutatedExchange);
            })
            .onErrorResume(ex -> {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            });
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                       .setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8))
                       .parseClaimsJws(token)
                       .getBody();
        } catch (Exception ex) {
            return null;
        }
    }
}
