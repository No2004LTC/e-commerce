package ecommerce.example.ecommerce.infrastructure.payment.vietqr;

import ecommerce.example.ecommerce.domain.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class VietQRService {

    // THÔNG TIN TÀI KHOẢN (Bạn thay bằng STK thật của mình để demo quét cho xịn)
    private final String BANK_ID = "vcb"; 
    private final String ACCOUNT_NO = "1234567890"; // Thay số TK thật của bạn
    private final String ACCOUNT_NAME = "NGUYEN VAN A"; // Tên không dấu
    private final String TEMPLATE = "qr_only"; 

    public String createPaymentUrl(Order order) {
        try {
            String amount = String.valueOf(order.getTotalAmount().longValue());
            // Nội dung chuyển khoản: Thanh toan DH [ID đơn hàng]
            String description = URLEncoder.encode("Thanh toan DH " + order.getId().substring(0, 8), StandardCharsets.UTF_8);
            String accountNameEncoded = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8);

            // Link tạo ảnh QR chuẩn Napas
            String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
                    BANK_ID, ACCOUNT_NO, TEMPLATE, amount, description, accountNameEncoded);

            log.info("[VIETQR] Tạo thành công QR Code cho đơn hàng: {}", order.getId());
            return qrUrl;
        } catch (Exception e) {
            log.error("[VIETQR] Lỗi tạo QR: {}", e.getMessage());
            return null;
        }
    }
}