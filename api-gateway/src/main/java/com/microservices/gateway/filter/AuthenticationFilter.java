package com.microservices.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

  @Value("${jwt.secret}")
  private String secret;

  private static final List<String> OPEN_ENDPOINTS = List.of(
      "/api/auth/register",
      "/api/auth/login");

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getURI().getPath();

    if (isOpenEndpoint(path)) {
      return chain.filter(exchange);
    }

    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }

    String token = authHeader.substring(7);
    try {
      Claims claims = extractClaims(token);
      ServerHttpRequest modifiedRequest = request.mutate()
          .header("X-User-Id", claims.getSubject())
          .header("X-User-Role", claims.get("role", String.class))
          .build();
      return chain.filter(exchange.mutate().request(modifiedRequest).build());
    } catch (Exception e) {
      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
      return exchange.getResponse().setComplete();
    }
  }

  private boolean isOpenEndpoint(String path) {
    return OPEN_ENDPOINTS.stream().anyMatch(path::startsWith);
  }

  private Claims extractClaims(String token) {
    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
