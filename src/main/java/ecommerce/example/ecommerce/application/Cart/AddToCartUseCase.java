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
    private final ProductService productService;

    public Cart execute(String userId, String productIdStr, int quantity) {
        ProductId productId = ProductId.fromString(productIdStr);

        Product product = productService.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

       
        if (product.getOwnerId().equals(userId)) {
            throw new RuntimeException("Bạn không thể thêm sản phẩm của chính mình vào giỏ hàng!");
        }

       
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Kho hàng chỉ còn " + product.getStockQuantity() + " sản phẩm.");
        }

        CartItem newItem = new CartItem(
                product.getId().getValue(), 
                product.getName(),
                product.getPrice(), 
                quantity,
                product.getProductImageUrl()
        );

        Cart cart = cartGateway.findByUserId(userId)
                .orElse(new Cart(userId, new ArrayList<>()));
        
        cart.updateOrAddItem(newItem);
        cartGateway.save(cart);
        
        return cart;
    }
}