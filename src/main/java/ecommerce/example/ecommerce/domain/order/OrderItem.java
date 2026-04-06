package ecommerce.example.ecommerce.domain.order;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    private String productId;
    private String productName;
    private BigDecimal priceAtPurchase;
    private int quantity;
}