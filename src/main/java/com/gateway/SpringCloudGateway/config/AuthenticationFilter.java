package com.gateway.SpringCloudGateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final RouteValidator routeValidator;
    private final SecretKey signingKey;

    public AuthenticationFilter(RouteValidator routeValidator, @Value("${app.security.jwt.secret}") String secret) {
        this.routeValidator = routeValidator;
        this.signingKey = Keys.hmacShaKeyFor(sha256(secret));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (HttpMethod.OPTIONS.equals(request.getMethod()) || !routeValidator.isSecured.test(request)) {
            return chain.filter(exchange);
        }

        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authorization.substring(7);
        try {
            Claims claims = parseClaims(token);
            if (!"access".equals(claims.get("type", String.class))) {
                return unauthorized(exchange, "Refresh token cannot be used for API access");
            }
            if (claims.getSubject() == null || claims.getSubject().isBlank() || claims.getId() == null || claims.getId().isBlank()) {
                return unauthorized(exchange, "Token is missing required claims");
            }

            ServerHttpRequest mutatedRequest = request.mutate()
                    .headers(headers -> {
                        removeTrustedHeaders(headers);
                        headers.add("X-User-Id", claims.getSubject());
                        addIfPresent(headers, "X-Username", claims.get("username", String.class));
                        addIfPresent(headers, "X-User-Email", claims.get("email", String.class));
                        headers.add("X-Auth-Token-Id", claims.getId());
                        headers.add("X-Log-Id", buildLogId());
                        headers.add("X-User-Authorities", String.join(",", authorities(claims)));
                    })
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException | IllegalArgumentException ex) {
            logger.warn("JWT validation failed for {}: {}", request.getPath().value(), ex.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @SuppressWarnings("unchecked")
    private List<String> authorities(Claims claims) {
        Object rawAuthorities = claims.get("authorities");
        if (rawAuthorities instanceof List<?>) {
            return ((List<?>) rawAuthorities).stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of("ROLE_USER");
    }

    private void removeTrustedHeaders(HttpHeaders headers) {
        headers.remove("X-User-Id");
        headers.remove("X-Username");
        headers.remove("X-User-Email");
        headers.remove("X-Auth-Token-Id");
        headers.remove("X-User-Authorities");
        headers.remove("X-Log-Id");
    }

    private void addIfPresent(HttpHeaders headers, String name, String value) {
        if (value != null && !value.isBlank()) {
            headers.add(name, value);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"success\":false,\"message\":\"Unauthorized\",\"error\":{\"code\":\"AUTH_UNAUTHORIZED\",\"message\":\"Unauthorized\",\"details\":\""
                + escapeJson(message)
                + "\"}}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String buildLogId() {
        int randomNumber = new Random().nextInt(999999 - 100000 + 1) + 100000;
        return ("LG" + LocalDateTime.now().toLocalDate() + randomNumber).replace("-", "");
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
