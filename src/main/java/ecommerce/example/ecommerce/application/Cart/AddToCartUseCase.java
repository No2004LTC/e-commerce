package ecommerce.example.ecommerce.application.Cart;

import ecommerce.example.ecommerce.application.products.ProductService;
import ecommerce.example.ecommerce.domain.Cart.Cart;
import ecommerce.example.ecommerce.domain.Cart.CartItem;
import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AddToCartUseCase {
    private final CartGateway cartGateway;
    private final ProductService productService; // Dùng Service có sẵn của bạn

    public Cart execute(String userId, String productIdStr, int quantity) {
        // 1. Chuyển String sang ProductId domain object
        ProductId productId = ProductId.fromString(productIdStr);

        // 2. Tìm product qua ProductService
        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // 3. Tạo CartItem lấy thông tin chuẩn từ Entity Product
      CartItem newItem = new CartItem(
                product.getId().getValue(), 
                product.getName(),
                product.getPrice(), // Truyền BigDecimal trực tiếp ở đây
                quantity,
                product.getProductImageUrl()
        );

        // 4. Lưu vào Redis qua CartGateway
        Cart cart = cartGateway.findByUserId(userId)
                .orElse(new Cart(userId, new ArrayList<>()));
        
        cart.updateOrAddItem(newItem);
        cartGateway.save(cart);
        
        return cart;
    }
}