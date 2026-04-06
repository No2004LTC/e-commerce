package ecommerce.example.ecommerce.domain.order;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    private String id;
    private String buyerId;
    private String sellerId;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItem> items;
    private LocalDateTime createdAt;

    // Logic: Kiểm tra xem đơn hàng đã được thanh toán chưa
    public boolean isPaid() {
        return "PAID".equals(this.status);
    }
}