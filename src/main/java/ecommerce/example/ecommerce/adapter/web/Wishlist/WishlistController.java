package ecommerce.example.ecommerce.adapter.web.Wishlist;

import ecommerce.example.ecommerce.application.Wishlist.WishlistService;
import ecommerce.example.ecommerce.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

   @PostMapping("/{productId}")
public ResponseEntity<String> toggle(@PathVariable String productId) {
    String currentUserId = SecurityUtils.getCurrentUserId(); 
    if (currentUserId == null) return ResponseEntity.status(401).body("Vui lòng đăng nhập!");

    // Nhận kết quả từ service
    boolean isAdded = wishlistService.toggleWishlist(currentUserId, productId);

    if (isAdded) {
        return ResponseEntity.ok("Đã thêm vào danh sách yêu thích");
    } else {
        return ResponseEntity.ok("Đã xóa khỏi danh sách yêu thích");
    }
}
}