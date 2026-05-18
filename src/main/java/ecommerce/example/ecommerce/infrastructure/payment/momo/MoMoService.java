package ecommerce.example.ecommerce.infrastructure.payment.momo;

import ecommerce.example.ecommerce.domain.order.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoMoService {
    private final RestTemplate restTemplate;

    private final String partnerCode = "MOMO";
    private final String accessKey = "F8BF47B20ECC2DFF20CA";
    private final String secretKey = "fc6c7d6f367cd0de8d43dd2e30c2fabc";

    @Value("${momo.endpoint}")
    private String endpoint;
    // @Value("${momo.redirect-url}")
    private String redirectUrl = "https://momo.vn";
    // @Value("${momo.ipn-url}")
    private String ipnUrl = "https://momo.vn";

    public String createPaymentUrl(Order order) throws Exception {
        // 1. Chuẩn bị dữ liệu
        String time = String.valueOf(System.currentTimeMillis());
        String orderId = "ID" + time;
        String requestId = orderId; 
        long amountLong = order.getTotalAmount().longValue();
        String amountStr = String.valueOf(amountLong);
        String orderInfo = "Payment"; 
        String requestType = "captureWallet";
        String extraData = ""; 

        // 2. Chuỗi ký tên Alphabet chuẩn MoMo V2
        String rawHash = "accessKey=" + accessKey + 
                "&amount=" + amountStr + 
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl.trim() + 
                "&orderId=" + orderId + 
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode + 
                "&redirectUrl=" + redirectUrl.trim() +
                "&requestId=" + requestId + 
                "&requestType=" + requestType;

        String signature = MoMoSignature.generateSignature(secretKey, rawHash);
        log.info("[MOMO] RAW STRING: {}", rawHash);
        log.info("[MOMO] SIGNATURE: {}", signature);

        // 3. JSON Body: Phải dùng LinkedHashMap để giữ thứ tự Alphabet
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("requestId", requestId);
        body.put("amount", amountLong); // Gửi dạng số Long
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl.trim());
        body.put("ipnUrl", ipnUrl.trim());
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint.trim(), entity, Map.class);
            Map<String, Object> res = response.getBody();
            
            if (res != null && "0".equals(String.valueOf(res.get("resultCode")))) {
                log.info("--- [SUCCESS] PAYURL: {} ---", res.get("payUrl"));
                return (String) res.get("payUrl");
            }
            log.error("[MOMO REJECTED]: {}", res);
            throw new RuntimeException("MoMo rejected: " + (res != null ? res.get("message") : "Unknown"));
        } catch (Exception e) {
            log.error("[MOMO ERROR]: {}", e.getMessage());
            throw e;
        }
    }
}