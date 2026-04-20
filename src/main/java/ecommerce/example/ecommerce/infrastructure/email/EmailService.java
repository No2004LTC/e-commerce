package ecommerce.example.ecommerce.infrastructure.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    // Email đại diện của hệ thống - Lấy đúng mail của Long
    private final String FROM_EMAIL = "nguyenxuanlong10a1@gmail.com";

    /**
     * Gửi Email xác nhận thanh toán thành công (Xử lý bất đồng bộ)
     */
    @Async
    public void sendPaymentSuccessEmail(String toEmail, String orderId, long amount) {
        if (!isValidEmail(toEmail)) return;

        try {
            log.info("[MAIL SENDING] Đang gửi thư xác nhận thanh toán đến: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("LóngHop E-Commerce <" + FROM_EMAIL + ">");
            message.setTo(toEmail);
            message.setSubject("THANH TOÁN THÀNH CÔNG - ĐƠN HÀNG #" + orderId);
            message.setText("Chào bạn,\n\n" +
                    "Đơn hàng #" + orderId + " đã được thanh toán thành công.\n" +
                    "Số tiền: " + amount + " VND.\n" +
                    "Cảm ơn bạn đã tin tưởng mua sắm tại LóngHop!");

            mailSender.send(message);
            log.info("[MAIL SUCCESS] Thư thanh toán đã gửi đến: {}", toEmail);
        } catch (Exception e) {
            log.error("[MAIL FAILED] Lỗi khi gửi mail thanh toán đến {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Gửi Email chứa mã OTP để xác thực tài khoản (Xử lý bất đồng bộ)
     */
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        if (!isValidEmail(toEmail)) return;

        try {
            log.info("[OTP SENDING] Đang gửi mã OTP đến: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Hệ thống LóngHop <" + FROM_EMAIL + ">");
            message.setTo(toEmail);
            message.setSubject("MÃ XÁC THỰC OTP - LÓNGHOP");
            message.setText("Chào bạn,\n\n" +
                    "Mã xác thực (OTP) của bạn là: " + otpCode + "\n" +
                    "Mã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");

            mailSender.send(message);
            log.info("[OTP SUCCESS] Mã OTP đã được gửi đến: {}", toEmail);
        } catch (Exception e) {
            log.error("[OTP FAILED] Lỗi khi gửi OTP đến {}: {}", toEmail, e.getMessage());
        }
    }

    // Hàm kiểm tra email hợp lệ cơ bản
    private boolean isValidEmail(String email) {
        if (email == null || !email.contains("@")) {
            log.error("[MAIL ERROR] Email không hợp lệ: {}", email);
            return false;
        }
        return true;
    }
}