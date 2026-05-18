package ecommerce.example.ecommerce.domain.Category;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Category {
    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private CategoryId id;

    private String name;
    private String slug;
    private String description;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}