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
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    // Nó sẽ tự động lấy từ file application.properties của bạn
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Giải mã chuỗi Base64 từ file properties để làm Signing Key
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String userId) {
        return generateToken(username, userId, null);
    }

    /**
     * Sinh JWT token với role được nhúng vào claims.
     * role nên có prefix ROLE_ (để khớp với SimpleGrantedAuthority).
     * Ví dụ: "ROLE_ADMIN", "ROLE_SHOP_OWNER", "ROLE_USER".
     */
    public String generateToken(String username, String userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        if (role != null && !role.isBlank()) {
            // Đảm bảo luôn có prefix ROLE_
            claims.put("role", role.startsWith("ROLE_") ? role : "ROLE_" + role);
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // jwtExpiration của bạn đang là 3600000 (1 giờ)
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Các hàm extractUsername, extractUserId, isTokenValid giữ nguyên như cũ...
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * Extract role từ JWT claims (có prefix ROLE_, ví dụ: "ROLE_ADMIN").
     * Trả về null nếu role không có trong token (token cũ chưa embed role).
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}