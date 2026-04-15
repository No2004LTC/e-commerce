package ecommerce.example.ecommerce.infrastructure.payment.momo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat; // Có từ Java 17

public class MoMoSignature {
    public static String generateSignature(String secretKey, String data) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(dataBytes);
        
        // Java 17 cách dùng chuẩn nhất để tránh lỗi ffffff
        return HexFormat.of().formatHex(rawHmac);
    }
}