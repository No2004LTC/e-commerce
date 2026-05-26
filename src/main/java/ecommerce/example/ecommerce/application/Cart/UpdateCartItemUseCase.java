package ecommerce.example.ecommerce.application.Cart;

import ecommerce.example.ecommerce.domain.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * UseCase: Cập nhật số lượng của một sản phẩm trong giỏ hàng Redis.
 * - Nếu quantity <= 0: tự động xóa sản phẩm đó khỏi giỏ.
 * - Nếu quantity > 0: ghi đè số lượng mới.
 */
@Service
@RequiredArgsConstructor
public class UpdateCartItemUseCase {

    private final CartGateway cartGateway;

    /**
     * @param userId    Username / userId từ JWT Authentication
     * @param productId ID sản phẩm cần cập nhật số lượng
     * @param quantity  Số lượng mới (nếu <= 0 thì xóa sản phẩm đó)
     * @return Cart đã được cập nhật
     */
    public Cart execute(String userId, String productId, int quantity) {
        Cart cart = cartGateway.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại!"));

        if (quantity <= 0) {
            cart.removeItem(productId);
        } else {
            cart.updateQuantity(productId, quantity);
        }

        cartGateway.save(cart);
        return cart;
    }
}
