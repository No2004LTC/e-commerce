package ecommerce.example.ecommerce.domain.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal; // Thống nhất kiểu dữ liệu với Product

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem implements Serializable {
    private String productId;
    private String name;
    private BigDecimal price; 
    private int quantity;
    private String productAvatar;

    
    public BigDecimal getSubTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}