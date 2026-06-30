package com.rainbowforest.apigateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final SecretKey secretKey;

    /**
     * Danh sách path KHÔNG cần JWT (public endpoints)
     */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/login",
            "/api/v1/auth/login",
            "/api/v1/registration",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/validate-token",
            "/api/v1/auth/send-otp",
            "/api/v1/auth/verify-otp",
            "/api/v1/products" // Xem sản phẩm không cần login
    );

    /**
     * Path prefix mà customer (ROLE_USER) không cần token cũng xem được
     */
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/v1/products/", // Chi tiết sản phẩm
            "/api/v1/settings/" // Settings công khai
    );

    public JwtAuthFilter(
            @Value("${jwt.secret:RainbowForestSuperSecretKeyForJWT2025MustBe256Bits!!}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() {
        return -100; // Chạy trước các filter khác
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // OPTIONS (CORS preflight) → cho qua
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return chain.filter(exchange);
        }

        // Public endpoints → cho qua
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Lấy token từ header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Token không tồn tại. Vui lòng đăng nhập.");
        }

        String token = authHeader.substring(7);

        try {
            // Validate JWT
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = String.valueOf(claims.get("userId"));
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // Kiểm tra quyền truy cập theo path
            if (!isAuthorized(path, role)) {
                return forbidden(exchange, "Bạn không có quyền truy cập tài nguyên này.");
            }

            // Truyền thông tin user xuống các microservice qua header
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (ExpiredJwtException e) {
            return unauthorized(exchange, "Token đã hết hạn. Vui lòng đăng nhập lại.");
        } catch (JwtException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return unauthorized(exchange, "Token không hợp lệ.");
        }
    }

    /**
     * Kiểm tra path có phải public không
     */
    private boolean isPublicPath(String path) {
        // Exact match
        for (String p : PUBLIC_PATHS) {
            if (path.equals(p))
                return true;
        }
        // Prefix match
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix))
                return true;
        }
        return false;
    }

    /**
     * Kiểm tra role có được phép truy cập path không
     */
    private boolean isAuthorized(String path, String role) {
        // ADMIN → full quyền
        if ("ROLE_ADMIN".equals(role)) {
            return true;
        }

        // STAFF → được truy cập HRM (limited), orders, tables, bills, products,
        // inventory
        if ("ROLE_STAFF".equals(role)) {
            if (path.startsWith("/api/v1/hrm/"))
                return true;
            if (path.startsWith("/api/v1/orders"))
                return true;
            if (path.startsWith("/api/v1/tables"))
                return true;
            if (path.startsWith("/api/v1/bills"))
                return true;
            if (path.startsWith("/api/v1/products"))
                return true;
            if (path.startsWith("/api/v1/inventory"))
                return true;
            if (path.startsWith("/api/v1/users"))
                return true;
            if (path.startsWith("/api/v1/payments"))
                return true;
            // Chặn accounting, settings, analytics...
            return false;
        }

        // USER (customer) → chỉ được truy cập sản phẩm, giỏ hàng, đặt bàn, đơn hàng của
        // mình
        if ("ROLE_USER".equals(role)) {
            if (path.startsWith("/api/v1/products"))
                return true;
            if (path.startsWith("/api/v1/cart"))
                return true;
            if (path.startsWith("/api/v1/orders"))
                return true;
            if (path.startsWith("/api/v1/reservations"))
                return true;
            if (path.startsWith("/api/v1/users"))
                return true;
            if (path.startsWith("/api/v1/payments"))
                return true;
            if (path.startsWith("/api/v1/auth/"))
                return true;
            return false;
        }

        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return errorResponse(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return errorResponse(exchange, HttpStatus.FORBIDDEN, message);
    }

    private Mono<Void> errorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"message\":\"" + message + "\",\"status\":" + status.value() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}