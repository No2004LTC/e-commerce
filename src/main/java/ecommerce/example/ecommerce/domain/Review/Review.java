package ecommerce.example.ecommerce.domain.Review;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Review {
    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private ReviewId id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    private Integer rating;
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}