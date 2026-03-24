package ecommerce.example.ecommerce.domain.products;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @EmbeddedId
@AttributeOverride(name = "value", column = @Column(name = "id")) // Đè tên 'value' thành 'id' trong DB
private ProductId id;

    @Column(name = "product_code", unique = true, nullable = false)
    private String productCode;

    private String name;
    private String description;
    
    @Column(name = "product_image_url")
    private String productImageUrl;

    private String warehouse;
    private String supplier;
    private BigDecimal price;

    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "sold_quantity")
    private Integer soldQuantity;
}