package com.apigateway.filter;

import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Component
public class DebugAuthFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() { return -150; }
    private static final Logger logger = LoggerFactory.getLogger(DebugAuthFilter.class);
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    	 logger.info("---- DebugAuthFilter START ----");
         logger.info("Request path: {}", exchange.getRequest().getPath());
         logger.info("Cookies: {}", exchange.getRequest().getCookies());
         logger.info("Authorization header: {}", exchange.getRequest().getHeaders().getFirst("Authorization"));

        return ReactiveSecurityContextHolder.getContext()
            .doOnNext(ctx -> {
                if (ctx != null && ctx.getAuthentication() != null) {
                	 logger.info("Principal: {}", ctx.getAuthentication().getName());
                     logger.info("Authorities: {}", ctx.getAuthentication().getAuthorities());
                } else {
                	 logger.warn("SecurityContext empty or unauthenticated at this point.");
                }
            })
            .then(chain.filter(exchange))
            .doFinally(signal ->logger.info("---- DebugAuthFilter END ----"));
    }
}
