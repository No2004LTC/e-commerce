package ecommerce.example.ecommerce.domain.products;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product {
    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "id"))
    private ProductId id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId; 

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

    private String status; 
    public void validateAndDecreaseStock(int amount) {
        if (this.stockQuantity < amount) {
            throw new RuntimeException("Sản phẩm '" + this.name + "' không đủ hàng trong kho!");
        }
        this.stockQuantity -= amount;
        this.soldQuantity = (this.soldQuantity == null ? 0 : this.soldQuantity) + amount;
        
        if (this.stockQuantity == 0) {
            this.status = "OUT_OF_STOCK";
        }
    }
}