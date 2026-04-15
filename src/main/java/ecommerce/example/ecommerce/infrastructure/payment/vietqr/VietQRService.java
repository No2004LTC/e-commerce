package ecommerce.example.ecommerce.infrastructure.payment.vietqr;

import ecommerce.example.ecommerce.domain.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class VietQRService {

    // THÔNG TIN TÀI KHOẢN CỦA BẠN
    private final String BANK_ID = "vcb"; // Ngân hàng Vietcombank
    private final String ACCOUNT_NO = "1234567890"; // Thay bằng số TK VCB của bạn
    private final String ACCOUNT_NAME = "NGUYEN VAN A"; // Thay bằng tên chủ TK (không dấu)
    private final String TEMPLATE = "qr_only"; // Loại template (qr_only, compact, v.v.)

    public String createPaymentUrl(Order order) {
        String amount = String.valueOf(order.getTotalAmount().longValue());
        String description = URLEncoder.encode("Thanh toan don hang " + order.getId().substring(0, 8), StandardCharsets.UTF_8);
        String accountNameEncoded = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8);

        // Link tạo ảnh QR chuẩn Napas (VietQR)
        // Cấu trúc: https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NO>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<DESCRIPTION>&accountName=<NAME>
        String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
                BANK_ID, ACCOUNT_NO, TEMPLATE, amount, description, accountNameEncoded);

        log.info("[VIETQR] QR Code URL: {}", qrUrl);
        return qrUrl;
    }
}