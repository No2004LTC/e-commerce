package ecommerce.example.ecommerce.domain.Wishlist;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Wishlist {
    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private WishlistId id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}