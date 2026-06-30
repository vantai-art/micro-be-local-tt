package com.rainbowforest.userservice.controller;

import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    // ─── Forgot password via email link ───────────────────────────────────────

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập địa chỉ email"));
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            return ResponseEntity.badRequest().body(Map.of("message", "Địa chỉ email không hợp lệ"));
        try {
            passwordResetService.requestPasswordReset(email.trim().toLowerCase());
        } catch (Exception e) {
            System.err.println("[PasswordReset] Error: " + e.getMessage());
        }
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, chúng tôi đã gửi hướng dẫn đặt lại mật khẩu."));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        if (token == null || token.isBlank())
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Token không hợp lệ"));
        User user = passwordResetService.validateResetToken(token);
        if (user == null)
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Liên kết không hợp lệ hoặc đã hết hạn"));
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "firstName", user.getUserDetails() != null ? user.getUserDetails().getFirstName() : "bạn"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || token.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Token không hợp lệ"));
        if (newPassword == null || newPassword.length() < 4)
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu mới phải có ít nhất 4 ký tự"));
        boolean success = passwordResetService.resetPassword(token, newPassword);
        if (!success)
            return ResponseEntity.badRequest().body(Map.of("message", "Liên kết đã hết hạn hoặc đã được sử dụng."));
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công!"));
    }

    // ─── OTP flow ─────────────────────────────────────────────────────────────

    /**
     * Gửi OTP 6 số đến email đã đăng ký.
     * Request: { "email": "user@gmail.com" }
     */
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập email"));
        try {
            boolean sent = passwordResetService.sendOtp(email.trim().toLowerCase());
            if (!sent)
                return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, OTP đã được gửi."));
        } catch (Exception e) {
            System.err.println("[OTP] Error sending OTP: " + e.getMessage());
        }
        return ResponseEntity.ok(Map.of("message", "OTP đã được gửi đến email của bạn."));
    }

    /**
     * Xác thực OTP → trả về mật khẩu tạm để đăng nhập.
     * Request: { "email": "...", "otp": "123456" }
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        if (email == null || otp == null || otp.length() != 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Thông tin không hợp lệ"));

        String tempPassword = passwordResetService.verifyOtpAndGetTempPassword(
                email.trim().toLowerCase(), otp.trim());

        if (tempPassword == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Mã OTP không đúng hoặc đã hết hạn"));

        return ResponseEntity.ok(Map.of(
                "message", "Xác thực thành công!",
                "tempPassword", tempPassword));
    }

    // ─── Change password (yêu cầu JWT, user đã đăng nhập) ───────────────────

    /**
     * Đổi mật khẩu bằng mật khẩu cũ.
     * Header: X-User-Id (set bởi API Gateway từ JWT)
     * Request: { "currentPassword": "...", "newPassword": "..." }
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestBody Map<String, String> body) {

        if (userIdHeader == null || userIdHeader.isBlank())
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || currentPassword.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập mật khẩu hiện tại"));
        if (newPassword == null || newPassword.length() < 6)
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu mới phải có ít nhất 6 ký tự"));
        if (currentPassword.equals(newPassword))
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu mới phải khác mật khẩu hiện tại"));

        Long userId;
        try { userId = Long.parseLong(userIdHeader); }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thông tin người dùng không hợp lệ"));
        }

        String result = passwordResetService.changePasswordWithOld(userId, currentPassword, newPassword);
        return switch (result) {
            case "ok" -> ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
            case "wrong_password" -> ResponseEntity.badRequest()
                    .body(Map.of("message", "Mật khẩu hiện tại không đúng", "wrongPassword", true));
            default -> ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy tài khoản"));
        };
    }
}
