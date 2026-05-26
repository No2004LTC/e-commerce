package ecommerce.example.ecommerce.application.Cart;

import ecommerce.example.ecommerce.domain.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * UseCase: Lấy giỏ hàng hiện tại từ Redis.
 * Nếu user chưa có giỏ hàng, trả về giỏ rỗng (không throw exception).
 */
@Service
@RequiredArgsConstructor
public class GetCartUseCase {

    private final CartGateway cartGateway;

    /**
     * Lấy giỏ hàng theo userId. Trả về giỏ rỗng nếu chưa có.
     */
    public Cart execute(String userId) {
        return cartGateway.findByUserId(userId)
                .orElse(new Cart(userId, new ArrayList<>()));
    }

    /**
     * Xóa toàn bộ giỏ hàng của user (dùng khi clear cart hoặc sau khi đặt hàng).
     */
    public void clearCart(String userId) {
        cartGateway.deleteByUserId(userId);
    }
}