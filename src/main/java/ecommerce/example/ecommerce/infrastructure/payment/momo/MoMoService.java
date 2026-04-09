package ecommerce.example.ecommerce.infrastructure.payment.momo;

import ecommerce.example.ecommerce.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MoMoService {
    private final RestTemplate restTemplate;

    // Thông số Sandbox MoMo
    @Value("${momo.partner-code}")
    private final String partnerCode = "MOMOBKUN20220314";
    @Value("${momo.access-key}")
    private final String accessKey = "klm05ndA8h948C8f";
    @Value("${momo.secret-key}")
    private final String secretKey = "at67qH6mk8w5Y1n71y_your_secret"; // Thay bằng secret của bạn
    @Value("${momo.endpoint}")
    private final String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";

    public String createPaymentUrl(Order order) throws Exception {
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = order.getId();
        String amount = order.getTotalAmount().toPlainString();
        String orderInfo = "Thanh toán đơn hàng: " + orderId;
        String redirectUrl = "http://localhost:3000/thanks"; // Trang web của bạn
        String ipnUrl = "https://your-ngrok-link.app/api/payment/momo/callback"; // LINK NGROK CỦA BẠN
        String requestType = "captureWallet";
        String extraData = "";

        // Tạo chữ ký theo thứ tự Alphabet của Key
        String rawHash = "accessKey=" + accessKey + "&amount=" + amount + "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl + "&orderId=" + orderId + "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode + "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId + "&requestType=" + requestType;

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
        return (String) response.getBody().get("payUrl");
    }
}