package ecommerce.example.ecommerce.application.Review;

import ecommerce.example.ecommerce.domain.Review.Review;
import ecommerce.example.ecommerce.domain.Review.ReviewId;
import ecommerce.example.ecommerce.domain.Review.ReviewRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddProductReviewUseCase {
    private final ReviewRepository reviewRepository;

    @Getter @Builder
    public static class Command {
        private String productId;
        private String userId;
        private int rating;
        private String comment;
    }

    public void execute(Command command) {
        Review review = new Review(
            new ReviewId(UUID.randomUUID().toString()),
            command.getProductId(),
            command.getUserId(),
            command.getRating(),
            command.getComment(),
            null // created_at sẽ được @PrePersist xử lý
        );
        reviewRepository.save(review);
    }
}