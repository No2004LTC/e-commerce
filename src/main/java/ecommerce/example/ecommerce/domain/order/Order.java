package ecommerce.example.ecommerce.domain.order;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private String customerId;
    private String buyerId;
    private String sellerId;

    /**
     * Tên hiển thị chi nhánh bán — chỉ dùng cho serialization JSON/response,
     * KHÔNG map xuống DB (tránh lỗi Unknown column khi bảng chưa có cột này).
     * Giá trị được tính động từ LEFT JOIN users trong analytics query.
     */
    @Transient
    private String sellerName;

    @Column(name = "total_amount", precision = 20, scale = 2)
    private BigDecimal totalAmount;
    private String status;

    @Column(name = "payment_method")
    @Builder.Default
    private String paymentMethod = "CHUYỂN KHOẢN";

    @Transient
    @Builder.Default
    private String paymentStatus = "SUCCESS";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
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