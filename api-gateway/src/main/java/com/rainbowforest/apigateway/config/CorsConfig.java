package com.rainbowforest.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ========================================
 * CORS Configuration cho API Gateway
 * ========================================
 * FIX: Thêm http://localhost:3000 là origin chính của React app
 * khi chạy bằng XAMPP / localhost
 */
@Configuration
public class CorsConfig {

    // ✅ Danh sách origin được phép — thêm origin mới vào đây
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:3000",   // React (create-react-app)
            "http://localhost:3001",   // React alt port
            "http://localhost:5173",   // Vite dev server
            "http://localhost:8081",   // Expo Web / React Native Web
            "http://localhost:19006",  // Expo Web alt port
            "http://127.0.0.1:3000",   // Loopback React
            "http://127.0.0.1:5173",   // Loopback Vite
            "http://10.0.2.2:8081",    // Android Emulator
            "http://localhost:8080"    // Same origin (gateway itself)
    );

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        // Expose headers cần thiết cho frontend
        config.setExposedHeaders(Arrays.asList("Authorization", "Location", "Access-Control-Allow-Origin"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
