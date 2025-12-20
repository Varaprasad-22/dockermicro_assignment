package com.apigateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ReactiveAuthenticationEntryPoint
        implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException ex) {

        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] bytes = new ObjectMapper().writeValueAsBytes(
                    Map.of("error", "Unauthorized")
            );
            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(bytes))
            );
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
