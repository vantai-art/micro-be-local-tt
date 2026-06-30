package com.rainbowforest.hrmservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new GatewayHeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // ── Quy tắc phân quyền HRM ──────────────────────────────────

                        // GET nhân viên: ADMIN + STAFF đều xem được
                        .requestMatchers(HttpMethod.GET, "/hrm/employees/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/hrm/departments/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/hrm/positions/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")

                        // Chấm công: STAFF check-in/out cho chính mình, ADMIN xem tất cả
                        .requestMatchers(HttpMethod.POST, "/hrm/attendance/check-in")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/hrm/attendance/check-out")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/hrm/attendance/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/hrm/attendances/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")

                        // Nghỉ phép: STAFF gửi đơn, ADMIN duyệt
                        .requestMatchers(HttpMethod.GET, "/hrm/leaves/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/hrm/leaves/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/hrm/leave-requests/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/hrm/leave-requests/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers("/hrm/leave-requests/*/approve").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/hrm/leave-requests/*/reject").hasAuthority("ROLE_ADMIN")

                        // Tạo / sửa / xóa nhân viên, phòng ban, chức vụ: chỉ ADMIN
                        .requestMatchers(HttpMethod.POST, "/hrm/employees/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/hrm/employees/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/hrm/employees/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/hrm/employees/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/hrm/departments/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/hrm/departments/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/hrm/departments/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/hrm/positions/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/hrm/positions/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/hrm/positions/**").hasAuthority("ROLE_ADMIN")

                        // Chi nhánh & ca làm việc: xem tất cả, CRUD chỉ ADMIN
                        .requestMatchers(HttpMethod.GET, "/hrm/branches/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.GET, "/hrm/shifts/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")
                        .requestMatchers(HttpMethod.POST, "/hrm/branches/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/hrm/branches/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/hrm/branches/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/hrm/shifts/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/hrm/shifts/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/hrm/shifts/**").hasAuthority("ROLE_ADMIN")

                        // Bảng lương: chỉ ADMIN
                        .requestMatchers("/hrm/payrolls/**").hasAuthority("ROLE_ADMIN")

                        // Activity log: chỉ ADMIN xem được
                        .requestMatchers("/hrm/activity-logs/**").hasAuthority("ROLE_ADMIN")

                        // Xác nhận chấm công: chỉ ADMIN
                        .requestMatchers("/hrm/attendances/*/confirm").hasAuthority("ROLE_ADMIN")

                        // Tất cả request khác trong /hrm/** cần ít nhất STAFF
                        .requestMatchers("/hrm/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")

                        .anyRequest().permitAll());
        return http.build();
    }

    /**
     * Filter đọc header X-User-Role, X-User-Id từ API Gateway
     * và set vào SecurityContext để Spring Security kiểm tra quyền
     */
    static class GatewayHeaderAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            String role = request.getHeader("X-User-Role");
            String userId = request.getHeader("X-User-Id");
            String username = request.getHeader("X-User-Name");

            if (role != null && !role.isBlank()) {
                var authorities = List.of(new SimpleGrantedAuthority(role));
                var auth = new UsernamePasswordAuthenticationToken(
                        username != null ? username : userId,
                        null,
                        authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);
        }
    }
}