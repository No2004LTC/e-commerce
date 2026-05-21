package ecommerce.example.ecommerce.adapter.web.Review;

import ecommerce.example.ecommerce.application.Review.AddProductReviewUseCase;
import ecommerce.example.ecommerce.application.Review.GetProductReviewsUseCase;
import ecommerce.example.ecommerce.application.dto.ReviewResponseDTO;
import ecommerce.example.ecommerce.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ReviewController {

    private final AddProductReviewUseCase addProductReviewUseCase;
    private final GetProductReviewsUseCase getProductReviewsUseCase;

    // POST: Thêm review mới
    @PostMapping("/{id}/reviews")
    public ResponseEntity<String> addReview(
            @PathVariable String id, 
            @RequestBody ReviewRequest request) {
        
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

    // GET: Lấy danh sách review theo sản phẩm (Đã sửa đưa vào trong thân class)
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getProductReviews(@PathVariable String productId) {
        List<ReviewResponseDTO> reviews = getProductReviewsUseCase.execute(productId);
        return ResponseEntity.ok(reviews);
    }
}

@lombok.Data
class ReviewRequest {
    private int rating;
    private String comment;
}