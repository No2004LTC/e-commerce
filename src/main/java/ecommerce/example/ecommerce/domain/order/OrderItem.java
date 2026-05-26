package ecommerce.example.ecommerce.domain.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*; 
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private String productId;
    private String productName;
    private BigDecimal priceAtPurchase;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;
}