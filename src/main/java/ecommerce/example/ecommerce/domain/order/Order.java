package ecommerce.example.ecommerce.domain.order;

import jakarta.persistence.*; 
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity 
@Table(name = "orders") 
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id // Khóa chính
    private String id;
    
    private String buyerId;
    private String sellerId;
    private BigDecimal totalAmount;
    private String status;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") 
    private List<OrderItem> items;

    private LocalDateTime createdAt;

    @PrePersist 
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isPaid() {
        return "PAID".equals(this.status);
    }
}