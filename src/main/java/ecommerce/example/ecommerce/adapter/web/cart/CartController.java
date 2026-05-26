package ecommerce.example.ecommerce.adapter.web.cart;

import ecommerce.example.ecommerce.application.Cart.AddToCartUseCase;
import ecommerce.example.ecommerce.application.Cart.GetCartUseCase;
import ecommerce.example.ecommerce.application.Cart.RemoveFromCartUseCase;
import ecommerce.example.ecommerce.application.Cart.UpdateCartItemUseCase;
import ecommerce.example.ecommerce.domain.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller cho Giỏ hàng Redis.
 *
 * userId được bóc tách từ JWT Token qua authentication.getName()
 * (Spring Security ghi username vào principal sau khi xác thực JWT).
 *
 * Endpoints:
 *   POST   /api/cart           — Thêm hoặc tăng số lượng sản phẩm
 *   GET    /api/cart           — Xem giỏ hàng hiện tại
 *   PUT    /api/cart/{id}      — Cập nhật số lượng sản phẩm
 *   DELETE /api/cart/{id}      — Xóa một sản phẩm khỏi giỏ
 *   DELETE /api/cart           — Xóa toàn bộ giỏ hàng (clear cart)
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final AddToCartUseCase addToCartUseCase;
    private final GetCartUseCase getCartUseCase;
    private final RemoveFromCartUseCase removeFromCartUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;

    // =========================================================================
    // POST /api/cart — Thêm sản phẩm vào giỏ (hoặc tăng số lượng nếu đã có)
    // Body: { "productId": "...", "quantity": 1 }
    // =========================================================================
    @PostMapping
    public ResponseEntity<Cart> addToCart(
            @RequestBody CartItemRequest request,
            Authentication auth) {
        String userId = auth.getName(); // Bóc tách username từ JWT SecurityContext
        Cart cart = addToCartUseCase.execute(userId, request.productId(), request.quantity());
        return ResponseEntity.ok(cart);
    }

    // =========================================================================
    // GET /api/cart — Lấy chi tiết giỏ hàng hiện tại của user đang đăng nhập
    // =========================================================================
    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication auth) {
        String userId = auth.getName();
        Cart cart = getCartUseCase.execute(userId);
        return ResponseEntity.ok(cart);
    }

    // =========================================================================
    // PUT /api/cart/{productId} — Cập nhật số lượng một sản phẩm cụ thể
    // Body: { "quantity": 3 }  — nếu quantity = 0 thì tự động xóa sản phẩm đó
    // =========================================================================
    @PutMapping("/{productId}")
    public ResponseEntity<Cart> updateQuantity(
            @PathVariable String productId,
            @RequestBody Map<String, Integer> body,
            Authentication auth) {
        String userId = auth.getName();
        int quantity = body.getOrDefault("quantity", 0);
        Cart cart = updateCartItemUseCase.execute(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    // =========================================================================
    // DELETE /api/cart/{productId} — Xóa một sản phẩm khỏi giỏ
    // =========================================================================
    @DeleteMapping("/{productId}")
    public ResponseEntity<Cart> removeFromCart(
            @PathVariable String productId,
            Authentication auth) {
        String userId = auth.getName();
        Cart cart = removeFromCartUseCase.execute(userId, productId);
        return ResponseEntity.ok(cart);
    }

    // =========================================================================
    // DELETE /api/cart — Xóa toàn bộ giỏ hàng (dùng khi hủy đơn hoặc logout)
    // =========================================================================
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCart(Authentication auth) {
        String userId = auth.getName();
        // Trả về giỏ rỗng, không throw nếu giỏ không tồn tại
        getCartUseCase.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "Giỏ hàng đã được xóa thành công."));
    }
}