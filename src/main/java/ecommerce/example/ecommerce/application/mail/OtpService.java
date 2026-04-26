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

   
    public void generateAndSendOtp(String email) {
        
        String otpCode = String.format("%06d", secureRandom.nextInt(1000000));

        
        redisTemplate.opsForValue().set("OTP_" + email, otpCode, 5, TimeUnit.MINUTES);

        
        emailService.sendOtpEmail(email, otpCode);
    }

    
    public boolean verifyOtp(String email, String userInputOtp) {
        String storedOtp = redisTemplate.opsForValue().get("OTP_" + email);
        
        if (storedOtp != null && storedOtp.equals(userInputOtp)) {
           
            redisTemplate.delete("OTP_" + email);
            return true;
        }
        return false;
    }
}