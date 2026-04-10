package ecommerce.example.ecommerce.infrastructure.payment.momo;

import ecommerce.example.ecommerce.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MoMoService {
    private final RestTemplate restTemplate;

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    public String createPaymentUrl(Order order) throws Exception {
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = order.getId();
        String amount = order.getTotalAmount().toPlainString();
        String orderInfo = "Thanh toán đơn hàng: " + orderId;
        String requestType = "captureWallet";
        String extraData = "";

        // Tạo chuỗi raw hash theo đúng thứ tự Alphabet của Key để làm chữ ký
        String rawHash = "accessKey=" + accessKey + 
                "&amount=" + amount + 
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl + 
                "&orderId=" + orderId + 
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode + 
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId + 
                "&requestType=" + requestType;

        String signature = MoMoSignature.generateSignature(secretKey, rawHash);

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("accessKey", accessKey);
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);
        body.put("lang", "vi");

        ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, body, Map.class);
        
        if (response.getBody() != null && response.getBody().containsKey("payUrl")) {
            return (String) response.getBody().get("payUrl");
        }
        
        throw new RuntimeException("Không thể lấy payUrl từ MoMo: " + response.getBody());
    }
}