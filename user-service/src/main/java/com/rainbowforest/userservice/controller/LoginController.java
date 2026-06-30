package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.repository.UserRepository;
import com.rainbowforest.userservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping({ "/login", "/auth/login" })
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String userName = body.get("userName");
        if (userName == null)
            userName = body.get("username");
        String userPassword = body.get("userPassword");
        if (userPassword == null)
            userPassword = body.get("password");

        // Validate input
        if (userName == null || userPassword == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Vui lòng nhập tên đăng nhập và mật khẩu"));
        }

        // Tìm user
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu"));
        }

        // Kiểm tra password — hỗ trợ cả BCrypt hash và plain text (để tương thích dữ
        // liệu cũ)
        boolean passwordMatch = false;
        if (user.getUserPassword().startsWith("$2a$") || user.getUserPassword().startsWith("$2b$")) {
            // Password đã được hash bằng BCrypt
            passwordMatch = passwordEncoder.matches(userPassword, user.getUserPassword());
        } else {
            // Password cũ dạng plain text — so sánh trực tiếp
            passwordMatch = user.getUserPassword().equals(userPassword);
            // Tự động nâng cấp: hash lại password cũ và lưu vào DB
            if (passwordMatch) {
                user.setUserPassword(passwordEncoder.encode(userPassword));
                userRepository.save(user);
            }
        }

        if (!passwordMatch) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sai tên đăng nhập hoặc mật khẩu"));
        }

        // Kiểm tra tài khoản bị khóa
        if (user.getActive() == 0) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Tài khoản đã bị khóa. Vui lòng liên hệ admin."));
        }

        // Tạo JWT token
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "ROLE_USER";
        String token = jwtUtil.generateToken(user.getId(), user.getUserName(), roleName);

        // Trả về response
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("userName", user.getUserName());
        result.put("active", user.getActive());
        result.put("userDetails", user.getUserDetails());
        result.put("role", roleName);
        result.put("token", token);
        result.put("mustChangePassword", user.isMustChangePassword());

        return ResponseEntity.ok(result);
    }
}