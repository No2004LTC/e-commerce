package ecommerce.example.ecommerce.domain.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal; // Thống nhất kiểu dữ liệu với Product

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable {
    private String productId;
    private String name;
    private BigDecimal price; // Dùng BigDecimal cho chuẩn tiền tệ
    private int quantity;
    private String productAvatar;

    // Hàm này giúp bạn lấy tổng tiền của dòng này (ví dụ: 2 cái iPhone)
    public BigDecimal getSubTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}