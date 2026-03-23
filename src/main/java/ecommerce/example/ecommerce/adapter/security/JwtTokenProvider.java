package ecommerce.example.ecommerce.adapter.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    // Key này sẽ được khởi tạo ngẫu nhiên trong bộ nhớ khi App chạy để đảm bảo an toàn tối đa
    private Key key;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Khởi tạo Key ngẫu nhiên khi ứng dụng bắt đầu.
     * Đảm bảo Key là duy nhất và cố định trong suốt vòng đời của Server (Runtime).
     */
    @PostConstruct
    public void init() {
        // Tạo key chuẩn 256-bit ngẫu nhiên phù hợp với thuật toán HS256
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    /**
     * Trả về chìa khóa ký đã được khởi tạo ngẫu nhiên.
     */
    private Key getSigningKey() {
        return this.key;
    }

    /**
     * Tạo mã thông báo JWT chứa cả Username và UserId.
     * @param username Tên đăng nhập của người dùng.
     * @param userId ID định danh của người dùng (dùng cho các UseCase như upload avatar).
     */
    public String generateToken(String username, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Trích xuất tên người dùng (username) từ mã thông báo.
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Trích xuất UserId từ mã thông báo.
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * Kiểm tra tính hợp lệ của mã thông báo.
     * Trả về true nếu mã hợp lệ và chưa hết hạn.
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // Token không hợp lệ hoặc đã hết hạn
            return false;
        }
    }

    /**
     * Giải mã và đọc toàn bộ thông tin (claims) trong mã thông báo.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}