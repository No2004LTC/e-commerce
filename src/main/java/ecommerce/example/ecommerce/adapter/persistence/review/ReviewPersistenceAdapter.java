package ecommerce.example.ecommerce.adapter.persistence.review;

import ecommerce.example.ecommerce.domain.Review.Review;
import ecommerce.example.ecommerce.domain.Review.ReviewId;
import ecommerce.example.ecommerce.domain.Review.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewPersistenceAdapter implements ReviewRepository {
    private final ReviewJpaRepository repository;

    @Override
    public void save(Review review) {
        repository.save(review);
    }

    @Override
    public List<Review> findByProductId(String productId) {
        return repository.findByProductId(productId);
    }

    @Override
    public Optional<Double> getAverageRatingByProductId(String productId) {
        return repository.getAverageRating(productId);
    }

    @Override
    public void delete(ReviewId id) {
        repository.deleteById(id);
    }
}