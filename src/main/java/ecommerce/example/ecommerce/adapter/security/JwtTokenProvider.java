package ecommerce.example.ecommerce.adapter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
    private String secretKey;

   @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Chuyển đổi chuỗi Secret Key từ dạng Base64
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Hàm công khai để tạo Token chỉ dựa vào tên người dùng .
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>(); 
        return createToken(claims, username, jwtExpiration);
    }

    /**
     * Quy trình xây dựng mã thông báo JWT:
     * - setClaims: Các thông tin mở rộng.
     * - setSubject: Thông tin định danh chính (thường là username).
     * - setIssuedAt: Thời điểm tạo token.
     * - setExpiration: Thời điểm token hết hạn.
     * - signWith: Ký xác nhận bằng thuật toán HS256 và chìa khóa bí mật.
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact(); 
    }

    /**
     * Trích xuất tên người dùng (username) từ trong chuỗi mã thông báo JWT.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Kiểm tra tính hợp lệ của Token:
     * - Nếu giải mã thành công bằng chìa khóa bí mật -> Token thật (true).
     * - Nếu lỗi (hết hạn, sai chữ ký, format lỗi) -> Token giả hoặc hỏng (false).
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Log lỗi tại đây nếu cần (ví dụ: e.getMessage())
            return false;
        }
    }

    /**
     * Đọc toàn bộ nội dung của mã thông báo.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}