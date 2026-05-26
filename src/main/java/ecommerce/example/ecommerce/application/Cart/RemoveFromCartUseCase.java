package ecommerce.example.ecommerce.application.Cart;

import ecommerce.example.ecommerce.domain.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * UseCase: Xóa một sản phẩm khỏi giỏ hàng Redis.
 * Nếu sau khi xóa giỏ rỗng, vẫn giữ lại entry trong Redis (giỏ rỗng).
 */
@Service
@RequiredArgsConstructor
public class RemoveFromCartUseCase {

    private final CartGateway cartGateway;

    /**
     * @param userId    Username / userId từ JWT Authentication
     * @param productId ID sản phẩm cần xóa khỏi giỏ
     * @return Cart sau khi đã xóa sản phẩm
     */
    public Cart execute(String userId, String productId) {
        Cart cart = cartGateway.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tồn tại hoặc đã trống!"));

        cart.removeItem(productId);
        cartGateway.save(cart);

        return cart;
    }
}
