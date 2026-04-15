package ecommerce.example.ecommerce.domain.Cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart implements Serializable {
    private String userId;
    private List<CartItem> items = new ArrayList<>();

    public void updateOrAddItem(CartItem newItem) {
        for (CartItem item : items) {
            if (item.getProductId().equals(newItem.getProductId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        this.items.add(newItem);
    }
    public void removeItem(String productId) {
        this.items.removeIf(item -> item.getProductId().equals(productId));
    }

    // Cập nhật số lượng (ví dụ khách chọn lại số lượng trên UI)
    public void updateQuantity(String productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }
    public BigDecimal getTotalPrice() {
    return items.stream()
            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
}