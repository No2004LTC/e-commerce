package ecommerce.example.ecommerce.adapter.web.cart;

import ecommerce.example.ecommerce.application.Cart.AddToCartUseCase;
import ecommerce.example.ecommerce.application.Cart.GetCartUseCase;
import ecommerce.example.ecommerce.application.dto.Card; 
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

    // CẬP NHẬT SỐ LƯỢNG 
    @PutMapping("/update")
    public ResponseEntity<Cart> updateQuantity(@RequestBody Card request, Authentication auth) {
        
        return ResponseEntity.ok(null); 
    }

    // XÓA SẢN PHẨM KHỎI GIỎ
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable String productId, Authentication auth) {
        return ResponseEntity.ok(null);
    }
}