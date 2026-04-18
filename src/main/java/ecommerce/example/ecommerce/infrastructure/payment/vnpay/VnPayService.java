package ecommerce.example.ecommerce.infrastructure.payment.vnpay;

import ecommerce.example.ecommerce.domain.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class VnPayService {

    private final String vnp_TmnCode = "2QXG2YHH";
    private final String vnp_HashSecret = "266B4NOHUADEDN7HOKTOSC1J90N3WJED";
    private final String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    @Value("${vnp.return-url:https://eery-nonmutinously-gertrude.ngrok-free.dev/api/payment/vnpay/return}")
    private String vnp_ReturnUrl;

    public String createPaymentUrl(Order order) throws Exception {
        String vnp_TxnRef = order.getId().replace("-", "").substring(0, 10) + (System.currentTimeMillis() % 1000);
        
        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(order.getTotalAmount().longValue() * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "ThanhToanDonHang" + vnp_TxnRef); // Bỏ dấu cách cho an toàn tuyệt đối
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        StringBuilder query = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = vnp_Params.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build Query: Quan trọng là URLEncode xong phải in hoa các ký tự %xx
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString())
                             .replace("+", "%20")); 
                
                if (itr.hasNext()) {
                    query.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        // TÍNH CHỮ KÝ TRÊN CHUỖI ĐÃ BUILD
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, queryUrl);
        String paymentUrl = vnp_Url + "?" + queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;

        log.info("[VNPAY] URL: {}", paymentUrl);
        return paymentUrl;
    }

    private String hmacSHA512(final String key, final String data) {
        try {
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString().toUpperCase(); // VIẾT HOA HASH
        } catch (Exception ex) {
            return "";
        }
    }
}