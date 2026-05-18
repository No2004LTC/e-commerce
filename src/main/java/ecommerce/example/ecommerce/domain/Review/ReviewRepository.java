package ecommerce.example.ecommerce.domain.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    void save(Review review);
    List<Review> findByProductId(String productId);
    Optional<Double> getAverageRatingByProductId(String productId);
    void delete(ReviewId id);
}