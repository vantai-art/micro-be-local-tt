package com.rainbowforest.userservice.service;

import com.rainbowforest.userservice.entity.OtpToken;
import com.rainbowforest.userservice.entity.PasswordResetToken;
import com.rainbowforest.userservice.entity.User;
import com.rainbowforest.userservice.repository.OtpTokenRepository;
import com.rainbowforest.userservice.repository.PasswordResetTokenRepository;
import com.rainbowforest.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.reset.token.expiry.minutes:15}")
    private int tokenExpiryMinutes;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final String TEMP_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // ─── Forgot password via email link (existing flow) ───────────────────────

    public void requestPasswordReset(String email) {
        User foundUser = findUserByEmail(email);
        if (foundUser == null) return;

        tokenRepository.deleteByUserId(foundUser.getId());

        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);

        PasswordResetToken resetToken = new PasswordResetToken(token, foundUser, expiresAt);
        tokenRepository.save(resetToken);

        String firstName = foundUser.getUserDetails().getFirstName();
        emailService.sendPasswordResetEmail(email, firstName, token);
    }

    public User validateResetToken(String token) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) return null;

        PasswordResetToken resetToken = opt.get();
        if (resetToken.isUsed() || resetToken.isExpired()) return null;

        return resetToken.getUser();
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) return false;

        PasswordResetToken resetToken = opt.get();
        if (resetToken.isUsed() || resetToken.isExpired()) return false;

        User user = resetToken.getUser();
        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return true;
    }

    // ─── OTP flow ─────────────────────────────────────────────────────────────

    public boolean sendOtp(String email) {
        User user = findUserByEmail(email);
        if (user == null) return false;

        otpTokenRepository.deleteByEmail(email);

        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        OtpToken otpToken = new OtpToken(email, otp, expiresAt);
        otpTokenRepository.save(otpToken);

        String firstName = user.getUserDetails() != null ? user.getUserDetails().getFirstName() : "bạn";
        emailService.sendOtpEmail(email, firstName, otp);
        return true;
    }

    /**
     * Xác thực OTP → tạo mật khẩu tạm, đặt mustChangePassword=true.
     * @return mật khẩu tạm nếu OTP đúng, null nếu sai/hết hạn
     */
    public String verifyOtpAndGetTempPassword(String email, String otp) {
        Optional<OtpToken> opt = otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (opt.isEmpty()) return null;

        OtpToken otpToken = opt.get();
        if (otpToken.isUsed() || otpToken.isExpired()) return null;
        if (!otpToken.getOtp().equals(otp)) return null;

        User user = findUserByEmail(email);
        if (user == null) return null;

        // Tạo mật khẩu tạm 10 ký tự
        String tempPassword = generateTempPassword(10);
        user.setUserPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);

        return tempPassword;
    }

    /**
     * Đổi mật khẩu bằng mật khẩu cũ. Trả về: "ok", "wrong_password", "not_found"
     */
    public String changePasswordWithOld(Long userId, String currentPassword, String newPassword) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return "not_found";

        User user = opt.get();
        boolean matches;
        if (user.getUserPassword().startsWith("$2a$") || user.getUserPassword().startsWith("$2b$")) {
            matches = passwordEncoder.matches(currentPassword, user.getUserPassword());
        } else {
            matches = user.getUserPassword().equals(currentPassword);
        }
        if (!matches) return "wrong_password";

        user.setUserPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        return "ok";
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(u -> u.getUserDetails() != null
                        && email.equalsIgnoreCase(u.getUserDetails().getEmail()))
                .findFirst()
                .orElse(null);
    }

    private String generateTempPassword(int length) {
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(TEMP_CHARS.charAt(rng.nextInt(TEMP_CHARS.length())));
        }
        return sb.toString();
    }
}
