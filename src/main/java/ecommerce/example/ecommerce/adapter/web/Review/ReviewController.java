package ecommerce.example.ecommerce.adapter.web.Review;

import ecommerce.example.ecommerce.application.Review.AddProductReviewUseCase;
import ecommerce.example.ecommerce.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ReviewController {
    private final AddProductReviewUseCase addProductReviewUseCase;

    @PostMapping("/{id}/reviews")
public ResponseEntity<String> addReview(
        @PathVariable String id, 
        @RequestBody ReviewRequest request) {
    
    // LẤY ID THẬT TỪ TOKEN Ở ĐÂY
    String currentUserId = SecurityUtils.getCurrentUserId(); 

    if (currentUserId == null) {
        return ResponseEntity.status(401).body("Bạn cần đăng nhập để đánh giá!");
    }

    addProductReviewUseCase.execute(AddProductReviewUseCase.Command.builder()
            .productId(id)
            .userId(currentUserId)
            .rating(request.getRating())
            .comment(request.getComment())
            .build());

    return ResponseEntity.ok("Đánh giá của bạn đã được gửi thành công!");
}
}

// Request DTO cho Review
@lombok.Data
class ReviewRequest {
    private int rating;
    private String comment;
}