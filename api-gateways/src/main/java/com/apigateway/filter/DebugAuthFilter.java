package com.apigateway.filter;

import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

@Component
public class DebugAuthFilter implements GlobalFilter, Ordered {

    @Override public int getOrder() { return -150; } // after cookie->header (-200), before decision

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("---- DebugAuthFilter START ----");
        System.out.println("Request path: " + exchange.getRequest().getPath());
        System.out.println("Cookies: " + exchange.getRequest().getCookies());
        System.out.println("Authorization header: " + exchange.getRequest().getHeaders().getFirst("Authorization"));

        return ReactiveSecurityContextHolder.getContext()
            .doOnNext(ctx -> {
                if (ctx != null && ctx.getAuthentication() != null) {
                    System.out.println("Principal: " + ctx.getAuthentication().getName());
                    System.out.println("Authorities: " + ctx.getAuthentication().getAuthorities());
                } else {
                    System.out.println("SecurityContext empty or unauthenticated at this point.");
                }
            })
            .then(chain.filter(exchange))
            .doFinally(signal -> System.out.println("---- DebugAuthFilter END ----"));
    }
}
