package com.apigateway.filter;

import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import reactor.core.publisher.Mono;

@Component
public class CookieToAuthHeaderFilter implements GlobalFilter, Ordered {
    private static final String COOKIE_NAME = "AUTH_TOKEN";
    private static final String AUTH_HEADER = "Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var cookies = exchange.getRequest().getCookies();
        if (cookies != null && cookies.containsKey(COOKIE_NAME)) {
            var cookie = cookies.getFirst(COOKIE_NAME);
            if (cookie != null) {
                String token = cookie.getValue();
                if (token != null && !token.isBlank()) {
                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                        .header(AUTH_HEADER, "Bearer " + token)
                        .build();
                    return chain.filter(exchange.mutate().request(mutated).build());
                }
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200; // run before security filter
    }
}
