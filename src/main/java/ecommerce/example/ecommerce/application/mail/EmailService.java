package ecommerce.example.ecommerce.application.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // QUAN TRỌNG: Phải có dòng này để dùng log
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j 
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendPaymentSuccessEmail(String toEmail, String orderId, long amount) {
        if (toEmail == null || !toEmail.contains("@")) {
            log.error("[MAIL ERROR] Email không hợp lệ: {}", toEmail);
            return;
        }

        try {
            log.info("[MAIL SENDING] Đang gửi thư xác nhận đến: {}", toEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            // Nhớ thay email thật của bạn vào chỗ "email-cua-ban@gmail.com"
            message.setFrom("Hệ thống E-Commerce <email-cua-ban@gmail.com>");
            message.setTo(toEmail);
            message.setSubject("Thanh toán thành công đơn hàng #" + orderId);
            message.setText("Chào bạn,\n\n" +
                    "Đơn hàng #" + orderId + " đã được thanh toán thành công.\n" +
                    "Số tiền: " + amount + " VND.\n" +
                    "Cảm ơn bạn đã mua sắm!");

            mailSender.send(message);
            log.info("[MAIL SUCCESS] Thư đã bay đến: {}", toEmail);
        } catch (Exception e) {
            log.error("[MAIL FAILED] Lỗi khi gửi đến {}: {}", toEmail, e.getMessage());
        }
    }
}