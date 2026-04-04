package ecommerce.example.ecommerce.adapter.web.cart;

import ecommerce.example.ecommerce.application.Cart.AddToCartUseCase;
import ecommerce.example.ecommerce.application.Cart.GetCartUseCase;
import ecommerce.example.ecommerce.application.dto.Card; // Import cái record vừa tạo
import ecommerce.example.ecommerce.domain.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final AddToCartUseCase addToCartUseCase;
    private final GetCartUseCase getCartUseCase;

    // THÊM VÀO GIỎ
    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestBody Card request, Authentication auth) {
        return ResponseEntity.ok(addToCartUseCase.execute(auth.getName(), request.productId(), request.quantity()));
    }

    // XEM GIỎ HÀNG
    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication auth) {
        return ResponseEntity.ok(getCartUseCase.execute(auth.getName()));
    }

    // CẬP NHẬT SỐ LƯỢNG (Ví dụ: khách nhấn dấu + hoặc - trên giao diện)
    @PutMapping("/update")
    public ResponseEntity<Cart> updateQuantity(@RequestBody Card request, Authentication auth) {
        // Bạn có thể dùng chung AddToCartRequest vì nó cũng cần productId và quantity
        // Gọi UseCase Update (cần tạo thêm UseCase này nếu muốn đúng chuẩn)
        return ResponseEntity.ok(null); 
    }

    // XÓA SẢN PHẨM KHỎI GIỎ
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable String productId, Authentication auth) {
        // Gọi UseCase Remove
        return ResponseEntity.ok(null);
    }
}