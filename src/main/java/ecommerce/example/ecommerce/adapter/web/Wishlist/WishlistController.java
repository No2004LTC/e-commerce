package ecommerce.example.ecommerce.adapter.web.Wishlist;

import ecommerce.example.ecommerce.application.Wishlist.WishlistService;
import ecommerce.example.ecommerce.infrastructure.security.SecurityUtils;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<?> getWishlist() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).body("Vui lòng đăng nhập!");

        List<String> productIds = wishlistService.getWishlistProductIds(currentUserId);
        return ResponseEntity.ok(productIds);
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleWithBody(@RequestBody ToggleRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).body("Vui lòng đăng nhập!");

        boolean isAdded = wishlistService.toggleWishlist(currentUserId, request.getProductId());
        String status = isAdded ? "ADDED" : "REMOVED";
        return ResponseEntity.ok(new ToggleResponse(status, request.getProductId()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<String> toggle(@PathVariable String productId) {
        String currentUserId = SecurityUtils.getCurrentUserId(); 
        if (currentUserId == null) return ResponseEntity.status(401).body("Vui lòng đăng nhập!");

        boolean isAdded = wishlistService.toggleWishlist(currentUserId, productId);

        if (isAdded) {
            return ResponseEntity.ok("Đã thêm vào danh sách yêu thích");
        } else {
            return ResponseEntity.ok("Đã xóa khỏi danh sách yêu thích");
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToggleRequest {
        private String productId;
    }

    @Getter
    @AllArgsConstructor
    public static class ToggleResponse {
        private String status;
        private String productId;
    }
}