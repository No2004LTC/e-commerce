package ecommerce.example.ecommerce.infrastructure.payment.vietqr;

import ecommerce.example.ecommerce.domain.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class VietQRService {

    
    private final String BANK_ID = "vcb"; 
    private final String ACCOUNT_NO = "0362720865"; 
    private final String ACCOUNT_NAME = "NGUYEN VU LONG"; 
    private final String TEMPLATE = "qr_only"; 

    public String createPaymentUrl(Order order) {
        try {
            String amount = String.valueOf(order.getTotalAmount().longValue());
           
            String description = URLEncoder.encode("Thanh toan DH " + order.getId().substring(0, 8), StandardCharsets.UTF_8);
            String accountNameEncoded = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8);

            
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