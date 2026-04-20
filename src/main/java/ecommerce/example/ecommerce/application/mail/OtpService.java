package ecommerce.example.ecommerce.application.mail;

import ecommerce.example.ecommerce.infrastructure.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    // 1. Tạo và gửi OTP
    public void generateAndSendOtp(String email) {
        // Tạo mã 6 số ngẫu nhiên
        String otpCode = String.format("%06d", secureRandom.nextInt(1000000));

        // Lưu vào Redis với key là email, tồn tại trong 5 phút (300 giây)
        redisTemplate.opsForValue().set("OTP_" + email, otpCode, 5, TimeUnit.MINUTES);

        // Gửi qua email
        emailService.sendOtpEmail(email, otpCode);
    }

    // 2. Xác thực OTP
    public boolean verifyOtp(String email, String userInputOtp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP_" + email);
        
        if (storedOtp != null && storedOtp.equals(userInputOtp)) {
            // Xác thực thành công thì xóa mã trong Redis luôn
            redisTemplate.delete("OTP_" + email);
            return true;
        }
        return false;
    }
}