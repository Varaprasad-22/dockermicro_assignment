package com.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.oauth2.jwt.*;
import io.jsonwebtoken.io.Decoders;
import java.security.Key;
import io.jsonwebtoken.security.Keys;

@Configuration
public class JwtDecoderConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        Key key = Keys.hmacShaKeyFor(keyBytes);
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key).build();
        return decoder;
    }
}
